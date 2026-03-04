package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.meritcap.DTOs.requestDTOs.course.CourseRequestDto;
import com.meritcap.DTOs.responseDTOs.bulk.BulkUploadResponseDto;
import com.meritcap.DTOs.responseDTOs.bulk.BulkUploadStatusDto;
import com.meritcap.enums.ActiveStatus;
import com.meritcap.helper.ExcelHelper;
import com.meritcap.model.*;
import com.meritcap.repository.*;
import com.meritcap.service.BulkUploadService;
import com.meritcap.utils.NormalizationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Rewritten BulkUploadServiceImpl.
 *
 * Key improvements over original:
 * 1) Parse the Excel file ONCE via ExcelHelper.parseWorkbook(File) — no
 * triple-parse.
 * 2) Batch DB operations (colleges, courses, college-courses) instead of
 * row-by-row.
 * 3) Rich content fields (credits, aboutCourse, keyFeatures, etc.) applied to
 * CollegeCourse.
 * 4) faqsUniversity applied to College entity.
 * 5) Comprehensive per-row error tracking saved to bulk_upload_errors table.
 * 6) Progress tracking via BulkUploadJobRepository.
 */
@Service
public class BulkUploadServiceImpl implements BulkUploadService {

    private static final Logger log = LoggerFactory.getLogger(BulkUploadServiceImpl.class);

    private final BulkUploadJobRepository jobRepository;
    private final CollegeRepository collegeRepository;
    private final CourseRepository courseRepository;
    private final CollegeCourseRepository collegeCourseRepository;
    private final BulkUploadErrorRepository bulkUploadErrorRepository;
    private final Validator validator;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 500;
    private static final int PROGRESS_UPDATE_INTERVAL = 50;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public BulkUploadServiceImpl(BulkUploadJobRepository jobRepository,
            CollegeRepository collegeRepository,
            CourseRepository courseRepository,
            CollegeCourseRepository collegeCourseRepository,
            BulkUploadErrorRepository bulkUploadErrorRepository,
            Validator validator,
            EntityManager entityManager) {
        this.jobRepository = jobRepository;
        this.collegeRepository = collegeRepository;
        this.courseRepository = courseRepository;
        this.collegeCourseRepository = collegeCourseRepository;
        this.bulkUploadErrorRepository = bulkUploadErrorRepository;
        this.validator = validator;
        this.entityManager = entityManager;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public BulkUploadResponseDto startBulkUpload(MultipartFile file, String currentUserId) {
        Objects.requireNonNull(file, "file must not be null");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.xlsx");

        // Create job record
        BulkUploadJob job = BulkUploadJob.builder()
                .fileName(filename)
                .createdBy(currentUserId)
                .status(BulkUploadJob.Status.PENDING)
                .totalRecords(0)
                .processedRecords(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        BulkUploadJob saved = jobRepository.save(job);
        Long jobId = saved.getId();
        log.info("Bulk upload job created: {} by {} filename={}", jobId, currentUserId, filename);

        // Save to temp file
        File tmpFile;
        try {
            tmpFile = saveToTempFile(file, jobId);
        } catch (Exception e) {
            log.error("Failed to save uploaded file to temp for job {}: {}", jobId, e.getMessage(), e);
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.FAILED,
                    truncate(e.getMessage(), 2000), LocalDateTime.now());
            throw new IllegalStateException("Failed to store uploaded file");
        }

        // Submit async processing
        executor.submit(() -> processFileAsync(jobId, tmpFile, currentUserId, filename));

        return BulkUploadResponseDto.builder()
                .jobId(jobId)
                .fileName(filename)
                .status(saved.getStatus().name())
                .build();
    }

    @Override
    public BulkUploadStatusDto getStatus(Long jobId) {
        BulkUploadJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId));
        return toStatusDto(job);
    }

    @Override
    public List<BulkUploadStatusDto> listRecentJobs() {
        return jobRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toStatusDto)
                .collect(java.util.stream.Collectors.toList());
    }

    private BulkUploadStatusDto toStatusDto(BulkUploadJob job) {
        int pct = 0;
        if (job.getTotalRecords() != null && job.getTotalRecords() > 0) {
            pct = (int) Math.round(100.0 * job.getProcessedRecords() / job.getTotalRecords());
        }
        return BulkUploadStatusDto.builder()
                .jobId(job.getId())
                .fileName(job.getFileName())
                .totalRecords(job.getTotalRecords())
                .processedRecords(job.getProcessedRecords())
                .percentComplete(pct)
                .status(String.valueOf(job.getStatus()))
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    // =========================================================================
    // Async processing pipeline
    // =========================================================================

    private void processFileAsync(Long jobId, File tmpFile, String userId, String originalFilename) {
        long startMs = System.currentTimeMillis();
        log.info("PROCESS_START jobId={} user={} file={}", jobId, userId, originalFilename);

        jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.IN_PROGRESS, null, LocalDateTime.now());

        try {
            // ── STEP 1: Parse Excel file ONCE ──
            ExcelHelper.CombinedParseResult parseResult = ExcelHelper.parseWorkbook(tmpFile);

            // Persist any parse-level errors
            for (ExcelHelper.RowError re : parseResult.getRowErrors()) {
                persistError(originalFilename, re.getRowNumber(), "parse", null,
                        re.getSnippet(), re.getMessage());
            }

            List<College> parsedColleges = parseResult.getColleges();
            List<CourseRequestDto> parsedCourses = parseResult.getCourses();
            List<CollegeCourseRequestExcelDto> ccDtos = parseResult.getCollegeCourses();

            int totalRecords = ccDtos.size();
            jobRepository.updateTotalRecords(jobId, totalRecords, LocalDateTime.now());
            log.info("PARSED jobId={} sheet='{}' dataRows={} colleges={} courses={} ccRows={} parseErrors={}",
                    jobId, parseResult.getSheetName(), parseResult.getTotalDataRows(),
                    parsedColleges.size(), parsedCourses.size(), ccDtos.size(), parseResult.getRowErrors().size());

            if (totalRecords == 0) {
                String msg = parseResult.getRowErrors().isEmpty() ? null
                        : "Parsing resulted in 0 valid rows. " + parseResult.getRowErrors().size() + " error(s). " +
                                "First error: " + parseResult.getRowErrors().get(0).getMessage();
                jobRepository.updateStatusAndError(jobId,
                        ccDtos.isEmpty() && !parseResult.getRowErrors().isEmpty()
                                ? BulkUploadJob.Status.FAILED
                                : BulkUploadJob.Status.COMPLETED,
                        truncate(msg, 2000), LocalDateTime.now());
                cleanupTempFile(tmpFile, jobId);
                return;
            }

            // ── STEP 2: Upsert Colleges ──
            Set<String> campusCodes = ccDtos.stream()
                    .map(CollegeCourseRequestExcelDto::getCampusCode)
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toUpperCase())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, College> parsedCollegeMap = parsedColleges.stream()
                    .filter(c -> c.getCampusCode() != null)
                    .collect(Collectors.toMap(
                            c -> c.getCampusCode().trim().toUpperCase(),
                            c -> c,
                            (a, b) -> b, LinkedHashMap::new));

            Map<String, College> existingColleges = upsertColleges(jobId, originalFilename,
                    campusCodes, parsedCollegeMap);

            // ── STEP 3: Upsert Courses ──
            Map<String, Course> existingCourses = upsertCourses(jobId, originalFilename, parsedCourses);

            // ── STEP 4: Process CollegeCourse rows in batches ──
            processCollegeCourseRows(jobId, originalFilename, ccDtos, existingColleges, existingCourses);

            // ── DONE ──
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.COMPLETED, null, LocalDateTime.now());
            long durationMs = System.currentTimeMillis() - startMs;
            log.info("PROCESS_COMPLETE jobId={} totalRecords={} durationMs={}", jobId, totalRecords, durationMs);

        } catch (Exception e) {
            log.error("PROCESS_FAILED jobId={} error={}", jobId, e.getMessage(), e);
            try {
                entityManager.clear();
            } catch (Exception ignored) {
            }
            markJobFailedNewTx(jobId, truncate(
                    Optional.ofNullable(e.getMessage()).orElse("Unexpected error"), 2000));
        } finally {
            cleanupTempFile(tmpFile, jobId);
        }
    }

    // =========================================================================
    // Step 2: Upsert Colleges
    // =========================================================================

    private Map<String, College> upsertColleges(Long jobId, String filename,
            Set<String> campusCodes,
            Map<String, College> parsedCollegeMap) {
        if (campusCodes.isEmpty())
            return Collections.emptyMap();

        // Load existing colleges by campus code
        Map<String, College> existing = collegeRepository.findByCampusCodeIn(campusCodes).stream()
                .collect(Collectors.toMap(College::getCampusCode, c -> c, (a, b) -> a));

        List<College> toUpdate = new ArrayList<>();
        List<College> toCreate = new ArrayList<>();

        for (String cc : campusCodes) {
            College parsed = parsedCollegeMap.get(cc);
            College db = existing.get(cc);

            if (db != null && parsed != null) {
                // Update existing college with parsed data
                if (parsed.getName() != null && !parsed.getName().isBlank()) {
                    db.setName(parsed.getName());
                }
                db.setCampusName(NormalizationUtil.nullifyIfEmpty(parsed.getCampusName()));
                db.setWebsiteUrl(NormalizationUtil.nullifyIfEmpty(parsed.getWebsiteUrl()));
                db.setCollegeLogo(NormalizationUtil.nullifyIfEmpty(parsed.getCollegeLogo()));
                db.setCountry(NormalizationUtil.nullifyIfEmpty(parsed.getCountry()));
                db.setDescription(NormalizationUtil.nullifyIfEmpty(parsed.getDescription()));
                db.setRanking(NormalizationUtil.nullifyIfEmpty(parsed.getRanking()));
                db.setEstablishedYear(sanitizeYear(parsed.getEstablishedYear()));
                db.setCampusGalleryVideoLink(NormalizationUtil.nullifyIfEmpty(parsed.getCampusGalleryVideoLink()));
                // New field: FAQs University
                if (parsed.getFaqsUniversity() != null && !parsed.getFaqsUniversity().isBlank()) {
                    db.setFaqsUniversity(parsed.getFaqsUniversity());
                }
                if (db.getSlug() == null || db.getSlug().isBlank()) {
                    db.setSlug(NormalizationUtil.genSlug(db.getName(), db.getCampusName()));
                }
                db.setUpdatedAt(LocalDateTime.now());
                toUpdate.add(db);

            } else if (db == null) {
                // Create new college
                String name = (parsed != null && parsed.getName() != null && !parsed.getName().isBlank())
                        ? parsed.getName()
                        : cc;
                String campus = (parsed != null) ? NormalizationUtil.nullifyIfEmpty(parsed.getCampusName()) : null;

                College.CollegeBuilder builder = College.builder()
                        .campusCode(cc)
                        .name(name)
                        .slug(NormalizationUtil.genSlug(name, campus))
                        .status(ActiveStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now());

                if (parsed != null) {
                    builder.campusName(campus)
                            .websiteUrl(NormalizationUtil.nullifyIfEmpty(parsed.getWebsiteUrl()))
                            .collegeLogo(NormalizationUtil.nullifyIfEmpty(parsed.getCollegeLogo()))
                            .country(NormalizationUtil.nullifyIfEmpty(parsed.getCountry()))
                            .establishedYear(sanitizeYear(parsed.getEstablishedYear()))
                            .ranking(NormalizationUtil.nullifyIfEmpty(parsed.getRanking()))
                            .description(NormalizationUtil.nullifyIfEmpty(parsed.getDescription()))
                            .campusGalleryVideoLink(
                                    NormalizationUtil.nullifyIfEmpty(parsed.getCampusGalleryVideoLink()))
                            .faqsUniversity(NormalizationUtil.nullifyIfEmpty(parsed.getFaqsUniversity()));
                }
                College newCollege = builder.build();

                // Validate
                Set<ConstraintViolation<College>> violations = validator.validate(newCollege);
                if (!violations.isEmpty()) {
                    String msgs = violations.stream()
                            .map(v -> v.getPropertyPath() + " " + v.getMessage())
                            .collect(Collectors.joining("; "));
                    log.warn("College validation failed campusCode={}: {}", cc, msgs);
                    persistError(filename, null, "college", cc, null,
                            "Validation failed: " + msgs);
                    continue;
                }
                toCreate.add(newCollege);
            }
        }

        // Save updates
        if (!toUpdate.isEmpty()) {
            try {
                collegeRepository.saveAll(toUpdate);
                log.info("UPDATED_COLLEGES jobId={} count={}", jobId, toUpdate.size());
            } catch (Exception e) {
                log.error("Failed to update colleges jobId={}: {}", jobId, e.getMessage(), e);
                persistError(filename, null, "college", null, null,
                        "Failed to update existing colleges: " + e.getMessage());
                try {
                    entityManager.clear();
                } catch (Exception ignored) {
                }
            }
        }

        // Save creates
        if (!toCreate.isEmpty()) {
            try {
                collegeRepository.saveAll(toCreate);
                log.info("CREATED_COLLEGES jobId={} count={}", jobId, toCreate.size());
            } catch (Exception e) {
                log.error("Failed to create colleges jobId={}: {}", jobId, e.getMessage(), e);
                persistError(filename, null, "college", null, null,
                        "Failed to create colleges: " + e.getMessage());
                try {
                    entityManager.clear();
                } catch (Exception ignored) {
                }
            }
        }

        // Reload all colleges for the campus codes
        return collegeRepository.findByCampusCodeIn(campusCodes).stream()
                .collect(Collectors.toMap(College::getCampusCode, c -> c, (a, b) -> a));
    }

    // =========================================================================
    // Step 3: Upsert Courses
    // =========================================================================

    private Map<String, Course> upsertCourses(Long jobId, String filename,
            List<CourseRequestDto> parsedCourses) {
        // Build keys for all parsed courses
        Set<String> courseKeys = parsedCourses.stream()
                .map(c -> makeCourseKey(c.getName(), c.getDepartment(),
                        NormalizationUtil.normalizeGraduationLevel(c.getGraduationLevel())))
                .collect(Collectors.toSet());

        // Load existing courses
        Map<String, Course> existing = courseKeys.isEmpty() ? Collections.emptyMap()
                : courseRepository.findByCourseKeys(courseKeys).stream()
                        .collect(Collectors.toMap(
                                c -> makeCourseKey(c.getName(), c.getDepartment(), c.getGraduationLevel()),
                                c -> c, (a, b) -> a));

        // Create missing courses (deduplicate by normalized key)
        List<Course> toCreate = new ArrayList<>();
        Set<String> pendingKeys = new HashSet<>();
        for (CourseRequestDto dto : parsedCourses) {
            String normGrad = NormalizationUtil.normalizeGraduationLevel(dto.getGraduationLevel());
            String key = makeCourseKey(dto.getName(), dto.getDepartment(), normGrad);
            if (!existing.containsKey(key) && pendingKeys.add(key)) {
                Course c = Course.builder()
                        .name(normalizeField(dto.getName()))
                        .department(normalizeField(dto.getDepartment()))
                        .graduationLevel(normGrad)
                        .specialization(dto.getSpecialization() != null ? dto.getSpecialization().trim() : null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                toCreate.add(c);
            }
        }

        if (!toCreate.isEmpty()) {
            final int SUB_BATCH = 500;
            int totalCreated = 0;
            for (int start = 0; start < toCreate.size(); start += SUB_BATCH) {
                int end = Math.min(start + SUB_BATCH, toCreate.size());
                List<Course> subBatch = toCreate.subList(start, end);
                try {
                    courseRepository.saveAll(subBatch);
                    totalCreated += subBatch.size();
                    log.info("CREATED_COURSES_BATCH jobId={} batch={}-{} count={}",
                            jobId, start, end, subBatch.size());
                } catch (Exception e) {
                    log.warn("BATCH_CREATE_COURSES_FAILED jobId={} batch={}-{}: {}. Falling back.",
                            jobId, start, end, e.getMessage());
                    try {
                        entityManager.clear();
                    } catch (Exception ignored) {
                    }
                    // Fall back to individual saves for this sub-batch only
                    for (Course c : subBatch) {
                        try {
                            courseRepository.save(c);
                            totalCreated++;
                        } catch (Exception ce) {
                            log.debug("Individual course save failed name='{}': {}", c.getName(), ce.getMessage());
                            try {
                                entityManager.clear();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            log.info("CREATED_COURSES_TOTAL jobId={} total={}", jobId, totalCreated);

            // Reload
            existing = courseRepository.findByCourseKeys(courseKeys).stream()
                    .collect(Collectors.toMap(
                            c -> makeCourseKey(c.getName(), c.getDepartment(), c.getGraduationLevel()),
                            c -> c, (a, b) -> a));
        }

        return existing;
    }

    // =========================================================================
    // Step 4: Process CollegeCourse rows (BATCH optimized)
    // =========================================================================

    private void processCollegeCourseRows(Long jobId, String filename,
            List<CollegeCourseRequestExcelDto> ccDtos,
            Map<String, College> existingColleges,
            Map<String, Course> existingCourses) {

        // ── Pre-load ALL existing college-course mappings in ONE query ──
        Set<Long> allCollegeIds = existingColleges.values().stream()
                .map(College::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Map: "collegeId|courseId" → existing CollegeCourse entity
        Map<String, CollegeCourse> existingCcMap = new HashMap<>();
        if (!allCollegeIds.isEmpty()) {
            List<CollegeCourse> existingCcList = collegeCourseRepository.findByCollegeIdIn(allCollegeIds);
            for (CollegeCourse cc : existingCcList) {
                String ccKey = cc.getCollege().getId() + "|" + cc.getCourse().getId();
                existingCcMap.put(ccKey, cc);
            }
            log.info("PRE_LOADED_CC jobId={} existingCollegeCourses={}", jobId, existingCcMap.size());
        }

        int processed = 0;
        List<CollegeCourse> batchToSave = new ArrayList<>(BATCH_SIZE);
        List<BulkUploadError> errorBatch = new ArrayList<>();

        for (int idx = 0; idx < ccDtos.size(); idx++) {
            CollegeCourseRequestExcelDto dto = ccDtos.get(idx);
            int rowNumber = idx + 2; // +2 because idx is 0-based, and row 1 is header

            try {
                String campusCode = dto.getCampusCode();
                if (campusCode == null || campusCode.isBlank()) {
                    errorBatch.add(buildError(filename, rowNumber, "college_course", null,
                            dtoSnippet(dto), "Row " + rowNumber + ": Missing campus code"));
                    continue;
                }
                campusCode = campusCode.trim().toUpperCase();

                // Resolve college (from pre-loaded map)
                College college = existingColleges.get(campusCode);
                if (college == null) {
                    errorBatch.add(buildError(filename, rowNumber, "college", campusCode,
                            dtoSnippet(dto),
                            "Row " + rowNumber + ": College not found for campus code '" + campusCode + "'"));
                    continue;
                }

                // Resolve course (from pre-loaded map)
                String normGrad = NormalizationUtil.normalizeGraduationLevel(dto.getGraduationLevel());
                if (normGrad == null && dto.getGraduationLevel() != null) {
                    normGrad = dto.getGraduationLevel().trim().toUpperCase();
                }
                String courseKey = makeCourseKey(dto.getCourseName(), dto.getDepartment(), normGrad);
                Course course = existingCourses.get(courseKey);

                if (course == null) {
                    // Create course on-the-fly (rare — should be pre-loaded already)
                    try {
                        Course nc = Course.builder()
                                .name(dto.getCourseName())
                                .department(dto.getDepartment())
                                .graduationLevel(normGrad)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        courseRepository.save(nc);
                        String newKey = makeCourseKey(nc.getName(), nc.getDepartment(), nc.getGraduationLevel());
                        existingCourses.put(newKey, nc);
                        course = nc;
                    } catch (Exception ce) {
                        errorBatch.add(buildError(filename, rowNumber, "course", courseKey,
                                dtoSnippet(dto), "Row " + rowNumber + ": Failed to create course '" + courseKey + "': "
                                        + ce.getMessage()));
                        continue;
                    }
                }

                // Upsert CollegeCourse — lookup from pre-loaded map (NO DB query)
                String ccKey = college.getId() + "|" + course.getId();
                CollegeCourse ccEntity = existingCcMap.get(ccKey);

                if (ccEntity != null) {
                    // Update existing
                    applyDtoToEntity(ccEntity, dto);
                    ccEntity.setUpdatedAt(LocalDateTime.now());
                    batchToSave.add(ccEntity);
                } else {
                    // Create new
                    ccEntity = CollegeCourse.builder()
                            .college(college)
                            .course(course)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    applyDtoToEntity(ccEntity, dto);
                    if (ccEntity.getIntakeYear() == null) {
                        ccEntity.setIntakeYear(NormalizationUtil.defaultIntakeYearIfMissing(null));
                    }
                    batchToSave.add(ccEntity);
                    // Add to map so subsequent rows for same pair will update instead of duplicate
                    existingCcMap.put(ccKey, ccEntity);
                }

            } catch (Exception rowEx) {
                log.error("ROW_ERROR jobId={} row={}: {}", jobId, rowNumber, rowEx.getMessage(), rowEx);
                errorBatch.add(buildError(filename, rowNumber, "college_course", null,
                        dtoSnippet(dto), "Row " + rowNumber + ": " + rowEx.getMessage()));
            } finally {
                processed++;
            }

            // Flush batch when threshold reached
            if (batchToSave.size() >= BATCH_SIZE) {
                flushCollegeCourseBatch(jobId, batchToSave, errorBatch, filename);
                batchToSave.clear();
                jobRepository.updateProcessedRecords(jobId, processed, LocalDateTime.now());
                log.info("BATCH_SAVED jobId={} processed={}/{}", jobId, processed, ccDtos.size());
            }

            // Flush errors periodically
            if (errorBatch.size() >= 100) {
                flushErrorBatch(errorBatch);
            }
        }

        // Flush remaining
        if (!batchToSave.isEmpty()) {
            flushCollegeCourseBatch(jobId, batchToSave, errorBatch, filename);
            batchToSave.clear();
        }
        if (!errorBatch.isEmpty()) {
            flushErrorBatch(errorBatch);
        }

        // Final progress update
        jobRepository.updateProcessedRecords(jobId, processed, LocalDateTime.now());
        log.info("CC_ROWS_COMPLETE jobId={} processed={}", jobId, processed);
    }

    /**
     * Flush a batch of CollegeCourse entities via saveAll().
     * On failure, falls back to individual saves to isolate bad rows.
     */
    private void flushCollegeCourseBatch(Long jobId, List<CollegeCourse> batch,
            List<BulkUploadError> errorBatch, String filename) {
        try {
            collegeCourseRepository.saveAll(batch);
        } catch (Exception e) {
            log.warn("BATCH_SAVE_FAILED jobId={} batchSize={} error={}. Falling back to individual saves.",
                    jobId, batch.size(), e.getMessage());
            try {
                entityManager.clear();
            } catch (Exception ignored) {
            }
            // Fall back to individual saves
            for (CollegeCourse cc : batch) {
                try {
                    collegeCourseRepository.save(cc);
                } catch (Exception ie) {
                    String identifier = (cc.getCollege() != null ? cc.getCollege().getCampusCode() : "?")
                            + " | " + (cc.getCourse() != null ? cc.getCourse().getName() : "?");
                    errorBatch.add(buildError(filename, null, "college_course", identifier,
                            null, "Failed to save: " + ie.getMessage()));
                }
            }
        }
    }

    private void flushErrorBatch(List<BulkUploadError> errors) {
        try {
            bulkUploadErrorRepository.saveAll(errors);
        } catch (Exception e) {
            log.error("Failed to flush error batch: {}", e.getMessage());
        }
        errors.clear();
    }

    // =========================================================================
    // Apply DTO fields to CollegeCourse entity (including new rich content)
    // =========================================================================

    private void applyDtoToEntity(CollegeCourse e, CollegeCourseRequestExcelDto dto) {
        // Course URL
        e.setCourseUrl(NormalizationUtil.nullifyIfEmpty(dto.getCourseUrl()));

        // Duration → parse to integer months
        Integer dur = NormalizationUtil.parseDurationMonths(dto.getDuration());
        if (dur == null && dto.getDuration() != null && !dto.getDuration().isBlank()) {
            try {
                dur = Integer.parseInt(dto.getDuration().trim());
            } catch (Exception ignored) {
            }
        }
        e.setDuration(dur == null ? 0 : dur);

        // Intake months → enum list (only update if changed to avoid @ElementCollection
        // DELETE+INSERT)
        List<com.meritcap.enums.Month> newMonths = NormalizationUtil
                .parseIntakeMonthsToEnum(dto.getIntakeMonths());
        if (newMonths == null)
            newMonths = new ArrayList<>();
        Set<com.meritcap.enums.Month> existingSet = e.getIntakeMonths() != null
                ? new HashSet<>(e.getIntakeMonths())
                : Collections.emptySet();
        Set<com.meritcap.enums.Month> newSet = new HashSet<>(newMonths);
        if (!existingSet.equals(newSet)) {
            e.setIntakeMonths(newMonths);
        }

        // Intake year
        e.setIntakeYear(dto.getIntakeYear() == null ? 0 : dto.getIntakeYear());

        // Text fields
        e.setEligibilityCriteria(NormalizationUtil.nullifyIfEmpty(dto.getEligibilityCriteria()));
        e.setApplicationFee(NormalizationUtil.nullifyIfEmpty(dto.getApplicationFee()));
        e.setTuitionFee(NormalizationUtil.nullifyIfEmpty(dto.getTuitionFee()));
        e.setScholarshipEligible(NormalizationUtil.nullifyIfEmpty(dto.getScholarshipEligible()));
        e.setScholarshipDetails(NormalizationUtil.nullifyIfEmpty(dto.getScholarshipDetails()));
        e.setBacklogAcceptanceRange(NormalizationUtil.nullifyIfEmpty(dto.getBacklogAcceptanceRange()));
        e.setRemarks(NormalizationUtil.nullifyIfEmpty(dto.getRemarks()));

        // Test scores (Double values)
        e.setIeltsMinScore(dto.getIeltsMinScore());
        e.setIeltsMinBandScore(dto.getIeltsMinBandScore());
        e.setToeflMinScore(dto.getToeflMinScore());
        e.setToeflMinBandScore(dto.getToeflMinBandScore());
        e.setPteMinScore(dto.getPteMinScore());
        e.setPteMinBandScore(dto.getPteMinBandScore());
        e.setDetMinScore(dto.getDetMinScore());
        e.setGreMinScore(dto.getGreMinScore());
        e.setGmatMinScore(dto.getGmatMinScore());
        e.setSatMinScore(dto.getSatMinScore());
        e.setCatMinScore(dto.getCatMinScore());

        // Academic scores
        e.setMin10thScore(dto.getMin10thScore());
        e.setMinInterScore(dto.getMinInterScore());
        e.setMinGraduationScore(dto.getMinGraduationScore());

        // ── Rich content fields (new columns AN-AZ) ──
        e.setCredits(NormalizationUtil.nullifyIfEmpty(dto.getCredits()));
        e.setDetailedScholarshipInfo(NormalizationUtil.nullifyIfEmpty(dto.getDetailedScholarshipInfo()));
        e.setWhyChooseThisCourse(NormalizationUtil.nullifyIfEmpty(dto.getWhyChooseThisCourse()));
        e.setAboutCourse(NormalizationUtil.nullifyIfEmpty(dto.getAboutCourse()));
        e.setKeyFeatures(NormalizationUtil.nullifyIfEmpty(dto.getKeyFeatures()));
        e.setLearningOutcomes(NormalizationUtil.nullifyIfEmpty(dto.getLearningOutcomes()));
        e.setCourseHighlights(NormalizationUtil.nullifyIfEmpty(dto.getCourseHighlights()));
        e.setCareerOpportunity(NormalizationUtil.nullifyIfEmpty(dto.getCareerOpportunity()));
        e.setFaqsCourse(NormalizationUtil.nullifyIfEmpty(dto.getFaqsCourse()));
        e.setCoreModules(NormalizationUtil.nullifyIfEmpty(dto.getCoreModules()));
        e.setAssessmentMethods(NormalizationUtil.nullifyIfEmpty(dto.getAssessmentMethods()));
        e.setJobMarkets(NormalizationUtil.nullifyIfEmpty(dto.getJobMarkets()));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private BulkUploadError buildError(String fileName, Integer rowNumber, String entityType,
            String identifier, String rawData, String errorMessage) {
        return BulkUploadError.builder()
                .fileName(fileName)
                .rowNumber(rowNumber)
                .entityType(entityType)
                .identifier(identifier)
                .rawData(rawData)
                .errorMessage(errorMessage == null ? "" : errorMessage)
                .build();
    }

    private void persistError(String fileName, Integer rowNumber, String entityType,
            String identifier, String rawData, String errorMessage) {
        try {
            bulkUploadErrorRepository.save(buildError(fileName, rowNumber, entityType,
                    identifier, rawData, errorMessage));
        } catch (Exception ex) {
            log.error("Failed to persist bulk_upload_error: {}", ex.getMessage(), ex);
        }
    }

    private void maybeUpdateProgress(Long jobId, int processed) {
        if (processed > 0 && processed % PROGRESS_UPDATE_INTERVAL == 0) {
            jobRepository.incrementProcessedRecords(jobId, PROGRESS_UPDATE_INTERVAL, LocalDateTime.now());
        }
    }

    private String dtoSnippet(CollegeCourseRequestExcelDto dto) {
        if (dto == null)
            return null;
        return "campusCode=" + dto.getCampusCode()
                + ", course=" + dto.getCourseName()
                + ", dept=" + dto.getDepartment()
                + ", level=" + dto.getGraduationLevel()
                + ", duration=" + dto.getDuration();
    }

    private File saveToTempFile(MultipartFile file, Long jobId) throws Exception {
        String safeName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.xlsx")
                .replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        File tmp = Files.createTempFile("bulkjob-" + jobId + "-", "-" + safeName).toFile();
        try (InputStream in = file.getInputStream(); FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1)
                out.write(buf, 0, r);
        }
        tmp.deleteOnExit();
        return tmp;
    }

    private void cleanupTempFile(File f, Long jobId) {
        if (f == null)
            return;
        try {
            boolean deleted = f.delete();
            log.debug("CLEANUP_TMP jobId={} deleted={}", jobId, deleted);
        } catch (Exception ex) {
            log.warn("CLEANUP_TMP_FAILED jobId={}: {}", jobId, ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markJobFailedNewTx(Long jobId, String truncatedMsg) {
        try {
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.FAILED, truncatedMsg, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to update job FAILED for job {}: {}", jobId, e.getMessage(), e);
        }
    }

    private String makeCourseKey(String name, String dept, String level) {
        return normalizeKeyPart(name) + "|" + normalizeKeyPart(dept) + "|" + normalizeKeyPart(level);
    }

    /**
     * Normalize a field value for storage: strip invisible chars + trim whitespace
     */
    private String normalizeField(String s) {
        if (s == null)
            return null;
        return NormalizationUtil.stripInvisibleChars(s).trim();
    }

    /**
     * Normalize a key part for comparison: strip invisible chars + trim + NFC +
     * lowercase
     */
    private String normalizeKeyPart(String s) {
        if (s == null)
            return "";
        String cleaned = NormalizationUtil.stripInvisibleChars(s).toLowerCase();
        return Normalizer.normalize(cleaned, Normalizer.Form.NFC);
    }

    /**
     * Sanitize established year: null out values outside [1800, 2100] range
     * rather than letting them fail @Min/@Max validation.
     */
    private Integer sanitizeYear(Integer year) {
        if (year == null)
            return null;
        if (year < 1800 || year > 2100)
            return null;
        return year;
    }

    private String truncate(String s, int max) {
        if (s == null)
            return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

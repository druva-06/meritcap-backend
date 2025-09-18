// File: src/main/java/com/consultancy/education/service/impl/BulkUploadServiceImpl.java
package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.consultancy.education.DTOs.requestDTOs.course.CourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadResponseDto;
import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadStatusDto;
import com.consultancy.education.enums.ActiveStatus;
import com.consultancy.education.model.*;
import com.consultancy.education.repository.*;
import com.consultancy.education.service.BulkUploadService;
import com.consultancy.education.helper.ExcelHelper;
import com.consultancy.education.util.NormalizationUtil;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    // tunables
    private final int persistBatchSize = 500;
    private final int progressUpdateBatchSize = 50;

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

    @Override
    public BulkUploadResponseDto startBulkUpload(MultipartFile file, String currentUserId) {
        Objects.requireNonNull(file, "file must not be null");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.xlsx");

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

        // save to temp file to allow multiple InputStreams for Apache POI
        File tmpFile;
        try {
            tmpFile = saveToTempFile(file, jobId);
        } catch (Exception e) {
            log.error("Failed to save uploaded file to temp for job {} : {}", jobId, e.getMessage(), e);
            String msg = truncate(e.getMessage(), 2000);
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.FAILED, msg, LocalDateTime.now());
            throw new IllegalStateException("Failed to store uploaded file");
        }

        // asynchronous processing
        final File processingFile = tmpFile;
        executor.submit(() -> processFileAsync(jobId, processingFile, currentUserId, filename));

        return BulkUploadResponseDto.builder()
                .jobId(jobId)
                .fileName(filename)
                .status(saved.getStatus().name())
                .build();
    }

    @Override
    public BulkUploadStatusDto getStatus(Long jobId) {
        BulkUploadJob job = jobRepository.findById(jobId).orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId));
        int pct = 0;
        if (job.getTotalRecords() != null && job.getTotalRecords() > 0) {
            pct = (int)Math.round(100.0 * job.getProcessedRecords() / (double) job.getTotalRecords());
        }
        return new BulkUploadStatusDto(job.getId(), job.getTotalRecords(), job.getProcessedRecords(), pct, String.valueOf(job.getStatus()), job.getErrorMessage(), job.getCreatedAt(), job.getUpdatedAt());
    }

    private void processFileAsync(Long jobId, File tmpFile, String currentUserId, String originalFilename) {
        long startMs = System.currentTimeMillis();
        log.info("PROCESS_START jobId={} user={} file={} tmp={}", jobId, currentUserId, originalFilename, tmpFile.getAbsolutePath());

        // mark in progress
        jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.IN_PROGRESS, null, LocalDateTime.now());

        try {
            // parse sheets by opening three separate InputStreams from same file: college-courses, courses, colleges
            List<CollegeCourseRequestExcelDto> collegeCourseDtos;
            List<CourseRequestDto> courseDtosFromCourseSheet;
            List<College> parsedCollegesList;
            try (InputStream is1 = new FileInputStream(tmpFile);
                 InputStream is2 = new FileInputStream(tmpFile);
                 InputStream is3 = new FileInputStream(tmpFile)) {

                ExcelHelper.ParseResult<CollegeCourseRequestExcelDto> ccRes = ExcelHelper.parseCollegeCourses(is1);
                ExcelHelper.ParseResult<CourseRequestDto> cRes = ExcelHelper.parseCourses(is2);
                ExcelHelper.ParseResult<College> colRes = ExcelHelper.parseColleges(is3);

                collegeCourseDtos = ccRes.getItems();
                courseDtosFromCourseSheet = cRes.getItems();
                parsedCollegesList = colRes.getItems();

                // If parse error count equals total rows => treat as fatal (same as existing logic)
                if ((collegeCourseDtos == null || collegeCourseDtos.isEmpty()) && (courseDtosFromCourseSheet == null || courseDtosFromCourseSheet.isEmpty())) {
                    String firstErr = null;
                    if (ccRes.getRowErrors() != null && !ccRes.getRowErrors().isEmpty()) firstErr = ccRes.getRowErrors().get(0).toString();
                    else if (cRes.getRowErrors() != null && !cRes.getRowErrors().isEmpty()) firstErr = cRes.getRowErrors().get(0).toString();
                    String msg = "Parsing resulted in 0 parsed rows. Sample error: " + (firstErr == null ? "none" : firstErr);
                    log.error("PARSE_FAILURE jobId={} {}", jobId, msg);
                    markJobFailedNewTx(jobId, truncate(msg, 2000));
                    return;
                }
            }

            if (collegeCourseDtos == null) collegeCourseDtos = Collections.emptyList();
            if (courseDtosFromCourseSheet == null) courseDtosFromCourseSheet = Collections.emptyList();

            // Build parsedColleges map keyed by normalized campus code (only keep parsed colleges for referenced campus codes later)
            List<College> parsedColleges = Collections.emptyList();
            // we created parsedCollegesList in the try-with-resources above; but scope requires we re-parse or keep reference.
            // To avoid scope issues, re-open file for parsed colleges if null (safety). But earlier we populated parsedCollegesList.
            // For clear code, we will attempt to reuse parsedCollegesList if available.
            // (Note: parsedCollegesList is effectively set above and visible here.)
            // If it's null, set to empty list:
            parsedColleges = (parsedColleges == null) ? Collections.emptyList() : parsedColleges;
            // However, because parsedCollegesList is local inside try, ensure we had assigned it to parsedColleges variable above.
            // To make this robust, we will re-parse colleges if parsedColleges is empty but that's unnecessary given try block above.
            // Instead, use reflection of value assigned above:
            // (Simpler: declare parsedCollegesList outside and assign inside — already done above.)

            // The code above declared parsedCollegesList; use it:
            // (But to avoid confusing duplication, let's reference courseDtosFromCourseSheet and collegeCourseDtos as before.)
            // To get parsed collegeslist we must have it in scope. Since it was assigned, use a placeholder:
            // We will create parsedCollegeMap from calling ExcelHelper.parseColleges again is unnecessarily expensive.
            // But we had parsedCollegesList assigned earlier inside try; use it by re-declaring parsedCollegesList as final. For brevity, assume it's accessible.
            // To ensure correctness, replicate parsing again if needed — but keeping performance in mind, the initial try already parsed it.
            // For clarity in this file, we'll simply parse colleges again using tmpFile because tmpFile is small and parsing is cheap relative to DB operations.

            // (Safer approach: parse colleges again here)
            List<College> parsedCollegeListFinal;
            try (InputStream isCol = new FileInputStream(tmpFile)) {
                ExcelHelper.ParseResult<College> colRes = ExcelHelper.parseColleges(isCol);
                parsedCollegeListFinal = (colRes == null || colRes.getItems() == null) ? Collections.emptyList() : colRes.getItems();
            } catch (Exception ex) {
                parsedCollegeListFinal = Collections.emptyList();
                log.warn("Failed to re-parse colleges for mapping: {}", ex.getMessage());
            }

            // Prepare set of campus codes referenced by collegeCourse rows (normalized)
            Set<String> campusCodes = collegeCourseDtos.stream()
                    .map(CollegeCourseRequestExcelDto::getCampusCode)
                    .filter(Objects::nonNull)
                    .map(NormalizationUtil::normalizeCampusCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Build parsedCollegeMap only for referenced campusCodes (to limit inserts/updates)
            Map<String, College> parsedCollegeMap = parsedCollegeListFinal.stream()
                    .filter(c -> c.getCampusCode() != null)
                    .collect(Collectors.toMap(
                            c -> NormalizationUtil.normalizeCampusCode(c.getCampusCode()),
                            c -> c,
                            // if duplicate campus codes in file, prefer the latter row (overwrite)
                            (existing, replacement) -> replacement,
                            LinkedHashMap::new
                    ));
            // Keep only parsed entries for campus codes that appear in the collegeCourse sheet
            parsedCollegeMap.keySet().retainAll(campusCodes);

            int totalRecords = collegeCourseDtos.size();
            jobRepository.updateTotalRecords(jobId, totalRecords, LocalDateTime.now());
            log.info("JOB_UPDATED_TOTAL jobId={} totalRecords={}", jobId, totalRecords);

            if (totalRecords == 0) {
                jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.COMPLETED, null, LocalDateTime.now());
                log.info("NO_RECORDS jobId={} finishedImmediately", jobId);
                cleanupTempFile(tmpFile, jobId);
                return;
            }

            // --- PRELOAD existing Colleges by campus_code (from referenced campusCodes)
            Map<String, College> existingColleges = campusCodes.isEmpty()
                    ? Collections.emptyMap()
                    : collegeRepository.findByCampusCodeIn(campusCodes).stream().collect(Collectors.toMap(College::getCampusCode, c -> c));

            // --- MERGE parsed college data into existing DB colleges (only where campusCode matches)
            List<College> collegesToUpdate = new ArrayList<>();
            for (Map.Entry<String, College> entry : existingColleges.entrySet()) {
                String dbCampus = entry.getKey();
                College dbCollege = entry.getValue();
                String normDbCampus = NormalizationUtil.normalizeCampusCode(dbCampus);
                College parsed = parsedCollegeMap.get(normDbCampus);
                if (parsed != null) {
                    // Overwrite fields with parsed values, but PROTECT required fields: do NOT set name to null/blank
                    if (parsed.getName() != null && !parsed.getName().isBlank()) {
                        dbCollege.setName(parsed.getName());
                    } // else keep existing name (to avoid violating @NotBlank)
                    // overwrite other fields (allow null to clear)
                    dbCollege.setCampusName(NormalizationUtil.nullifyIfEmpty(parsed.getCampusName()));
                    dbCollege.setWebsiteUrl(NormalizationUtil.nullifyIfEmpty(parsed.getWebsiteUrl()));
                    dbCollege.setCollegeLogo(NormalizationUtil.nullifyIfEmpty(parsed.getCollegeLogo()));
                    dbCollege.setCountry(NormalizationUtil.nullifyIfEmpty(parsed.getCountry()));
                    dbCollege.setDescription(NormalizationUtil.nullifyIfEmpty(parsed.getDescription()));
                    dbCollege.setRanking(NormalizationUtil.nullifyIfEmpty(parsed.getRanking()));
                    dbCollege.setEstablishedYear(parsed.getEstablishedYear());
                    dbCollege.setCampusGalleryVideoLink(NormalizationUtil.nullifyIfEmpty(parsed.getCampusGalleryVideoLink()));
                    dbCollege.setUpdatedAt(LocalDateTime.now());
                    // do not overwrite campusCode
                    // backfill slug only if missing
                    if (dbCollege.getSlug() == null || dbCollege.getSlug().isBlank()) {
                        dbCollege.setSlug(NormalizationUtil.genSlug(dbCollege.getName(), dbCollege.getCampusName()));
                    }
                    collegesToUpdate.add(dbCollege);
                } else {
                    // If no parsed data for this campus, ensure slug backfill if missing (existing behavior)
                    if (dbCollege.getSlug() == null || dbCollege.getSlug().isBlank()) {
                        dbCollege.setSlug(NormalizationUtil.genSlug(dbCollege.getName(), dbCollege.getCampusName()));
                        dbCollege.setUpdatedAt(LocalDateTime.now());
                        collegesToUpdate.add(dbCollege);
                    }
                }
            }

            if (!collegesToUpdate.isEmpty()) {
                try {
                    collegeRepository.saveAll(collegesToUpdate);
                    log.info("MERGED_EXISTING_COLLEGES jobId={} updatedCount={}", jobId, collegesToUpdate.size());
                } catch (Exception e) {
                    log.error("Failed to save updated colleges for job {} : {}", jobId, e.getMessage(), e);
                    // don't fail the whole job; persist error and continue
                    persistError(originalFilename, null, "college", null, null, "Failed to save updated colleges: " + e.getMessage());
                    try { entityManager.clear(); } catch (Exception ex) { log.warn("clear EM failed: {}", ex.getMessage()); }
                }
            }

            // --- CREATE missing Colleges using parsed data if available, otherwise fallback minimal
            List<College> collegesToCreate = new ArrayList<>();
            for (String campusCode : campusCodes) {
                if (!existingColleges.containsKey(campusCode)) {
                    // prefer parsed college if present for this campusCode
                    College parsed = parsedCollegeMap.get(campusCode);
                    if (parsed != null) {
                        College toCreate = College.builder()
                                .campusCode(NormalizationUtil.normalizeCampusCode(parsed.getCampusCode()))
                                // name must not be blank; fallback to campusCode
                                .name((parsed.getName() == null || parsed.getName().isBlank()) ? campusCode : parsed.getName())
                                .campusName(NormalizationUtil.nullifyIfEmpty(parsed.getCampusName()))
                                .websiteUrl(NormalizationUtil.nullifyIfEmpty(parsed.getWebsiteUrl()))
                                .collegeLogo(NormalizationUtil.nullifyIfEmpty(parsed.getCollegeLogo()))
                                .country(NormalizationUtil.nullifyIfEmpty(parsed.getCountry()))
                                .establishedYear(parsed.getEstablishedYear())
                                .ranking(NormalizationUtil.nullifyIfEmpty(parsed.getRanking()))
                                .description(NormalizationUtil.nullifyIfEmpty(parsed.getDescription()))
                                .campusGalleryVideoLink(NormalizationUtil.nullifyIfEmpty(parsed.getCampusGalleryVideoLink()))
                                .slug((parsed.getName() != null && !parsed.getName().isBlank()) ? NormalizationUtil.genSlug(parsed.getName(), parsed.getCampusName()) : NormalizationUtil.genSlug(campusCode, null))
                                .status(ActiveStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        collegesToCreate.add(toCreate);
                    } else {
                        // fallback minimal college
                        College c = College.builder()
                                .campusCode(campusCode)
                                .name(campusCode)
                                .slug(NormalizationUtil.genSlug(campusCode, null))
                                .status(ActiveStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        collegesToCreate.add(c);
                    }
                }
            }

            if (!collegesToCreate.isEmpty()) {
                // validate & save (attempt to continue for valid ones)
                List<College> valid = new ArrayList<>();
                for (College c : collegesToCreate) {
                    Set<ConstraintViolation<College>> violations = validator.validate(c);
                    if (!violations.isEmpty()) {
                        String msgs = violations.stream().map(v -> v.getPropertyPath() + " " + v.getMessage()).collect(Collectors.joining("; "));
                        log.warn("College validation failed for campusCode={} : {}", c.getCampusCode(), msgs);
                        // persist as row-level error (no row number available here)
                        persistError(originalFilename, null, "college", c.getCampusCode(), null, "Validation failed during create: " + msgs);
                        continue;
                    }
                    valid.add(c);
                }
                if (!valid.isEmpty()) {
                    try {
                        collegeRepository.saveAll(valid);
                    } catch (Exception e) {
                        log.error("Unexpected error saving colleges for job {}: {}", jobId, e.getMessage(), e);
                        try { entityManager.clear(); } catch (Exception ex) { log.warn("clear EM failed: {}", ex.getMessage()); }
                        // persist error and continue
                        persistError(originalFilename, null, "college", null, null, "Failed saving created colleges: " + e.getMessage());
                    }
                    // reload existingColleges map after creations/updates
                    existingColleges = collegeRepository.findByCampusCodeIn(campusCodes).stream().collect(Collectors.toMap(College::getCampusCode, cc -> cc));
                }
            }

            // --- PRELOAD Courses by composite key from courseDtosFromCourseSheet
            Set<String> courseKeys = courseDtosFromCourseSheet.stream()
                    .map(c -> makeCourseKey(c.getName(), c.getDepartment(), NormalizationUtil.normalizeGraduationLevel(c.getGraduationLevel())))
                    .collect(Collectors.toSet());
            Map<String, Course> existingCourses = courseKeys.isEmpty()
                    ? Collections.emptyMap()
                    : courseRepository.findByCourseKeys(courseKeys).stream().collect(Collectors.toMap(c -> makeCourseKey(c.getName(), c.getDepartment(), c.getGraduationLevel()), c -> c));

            // --- CREATE missing Courses automatically (as requested)
            List<Course> coursesToCreate = new ArrayList<>();
            for (CourseRequestDto cr : courseDtosFromCourseSheet) {
                String normGrad = NormalizationUtil.normalizeGraduationLevel(cr.getGraduationLevel());
                String key = makeCourseKey(cr.getName(), cr.getDepartment(), normGrad);
                if (!existingCourses.containsKey(key)) {
                    Course newCourse = Course.builder()
                            .name(cr.getName())
                            .department(cr.getDepartment())
                            .graduationLevel(normGrad)
                            .specialization(cr.getSpecialization())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    coursesToCreate.add(newCourse);
                }
            }
            if (!coursesToCreate.isEmpty()) {
                courseRepository.saveAll(coursesToCreate);
                // reload existingCourses map
                Set<String> refreshedKeys = courseKeys;
                existingCourses = courseRepository.findByCourseKeys(refreshedKeys).stream().collect(Collectors.toMap(c -> makeCourseKey(c.getName(), c.getDepartment(), c.getGraduationLevel()), c -> c));
                log.info("CREATED_MISSING_COURSES jobId={} created={}", coursesToCreate.size(), coursesToCreate.size());
            }

            // --- Process each collegeCourse row and insert/update accordingly
            int processed = 0;
            for (int idx = 0; idx < collegeCourseDtos.size(); idx++) {
                CollegeCourseRequestExcelDto dto = collegeCourseDtos.get(idx);
                int rowNumber = idx + 1;
                try {
                    // normalize campus code
                    String campusCodeRaw = dto.getCampusCode();
                    String campusCode = NormalizationUtil.normalizeCampusCode(campusCodeRaw);
                    if (campusCode == null) {
                        String msg = "Missing campus code";
                        log.warn("SKIP_ROW jobId={} row={} reason={}", jobId, rowNumber, msg);
                        persistError(originalFilename, rowNumber, "parse", null, dtoToRaw(dto), msg);
                        processed++;
                        maybeUpdateProgress(jobId, 1);
                        continue;
                    }

                    // ensure college exists (created/preloaded)
                    College college = existingColleges.get(campusCode);
                    if (college == null) {
                        // create minimal college on the fly (should be rare because we created for referenced campusCodes earlier)
                        College nc = College.builder()
                                .campusCode(campusCode)
                                .name(campusCode)
                                .slug(NormalizationUtil.genSlug(campusCode, null))
                                .status(ActiveStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        try {
                            collegeRepository.save(nc);
                            existingColleges.put(nc.getCampusCode(), nc);
                            college = nc;
                        } catch (Exception ce) {
                            String msg = "Failed to create minimal college: " + ce.getMessage();
                            log.error("CREATE_COLLEGE_FAILED jobId={} row={} campusCode={} error={}", jobId, rowNumber, campusCode, ce.getMessage());
                            persistError(originalFilename, rowNumber, "college", campusCode, dtoToRaw(dto), msg);
                            processed++;
                            maybeUpdateProgress(jobId, 1);
                            continue;
                        }
                    }

                    // course lookup: build normalized grad level
                    String normGradLevel = NormalizationUtil.normalizeGraduationLevel(dto.getGraduationLevel());
                    String courseKey = makeCourseKey(dto.getCourseName(), dto.getDepartment(), normGradLevel);
                    Course course = existingCourses.get(courseKey);
                    if (course == null) {
                        // create course automatically (as requested)
                        Course newCourse = Course.builder()
                                .name(dto.getCourseName())
                                .department(dto.getDepartment())
                                .graduationLevel(normGradLevel)
                                .specialization(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        try {
                            courseRepository.save(newCourse);
                            // refresh existingCourses map entry
                            String k = makeCourseKey(newCourse.getName(), newCourse.getDepartment(), newCourse.getGraduationLevel());
                            existingCourses.put(k, newCourse);
                            course = newCourse;
                        } catch (Exception ce) {
                            String msg = "Failed to create course: " + ce.getMessage();
                            log.error("CREATE_COURSE_FAILED jobId={} row={} key={} error={}", jobId, rowNumber, courseKey, ce.getMessage());
                            persistError(originalFilename, rowNumber, "course", courseKey, dtoToRaw(dto), msg);
                            processed++;
                            maybeUpdateProgress(jobId, 1);
                            continue;
                        }
                    }

                    // Now upsert the CollegeCourse (match by college.id & course.id)
                    Optional<CollegeCourse> maybeExisting = Optional.empty();
                    try {
                        if (college.getId() != null && course.getId() != null) {
                            if (collegeCourseRepository.existsByCollegeIdAndCourseId(college.getId(), course.getId())) {
                                maybeExisting = collegeCourseRepository.findByCollegeIdAndCourseId(college.getId(), course.getId());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("existence check for college_course failed: {}", e.getMessage());
                    }

                    if (maybeExisting.isPresent()) {
                        CollegeCourse ccEntity = maybeExisting.get();
                        // update (overwrite all fields from DTO) - blanks in DTO will override DB fields to null/0 (except where we protect earlier)
                        applyCollegeCourseDtoToEntityOverwriting(ccEntity, dto);
                        ccEntity.setUpdatedAt(LocalDateTime.now());
                        collegeCourseRepository.save(ccEntity);
                    } else {
                        CollegeCourse cc = CollegeCourse.builder()
                                .college(college)
                                .course(course)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        applyCollegeCourseDtoToEntityOverwriting(cc, dto);
                        if (cc.getIntakeYear() == null) cc.setIntakeYear(NormalizationUtil.defaultIntakeYearIfMissing(null));
                        collegeCourseRepository.save(cc);
                    }

                } catch (Exception rowEx) {
                    log.error("ROW_ERROR jobId={} row={} error={}", jobId, rowNumber, rowEx.getMessage(), rowEx);
                    persistError(originalFilename, rowNumber, "college_course", null, dtoToRaw(dto), "Row processing failed: " + rowEx.getMessage());
                } finally {
                    processed++;
                    if (processed % progressUpdateBatchSize == 0) {
                        jobRepository.incrementProcessedRecords(jobId, progressUpdateBatchSize, LocalDateTime.now());
                        log.debug("PROGRESS_UPDATED jobId={} processed={}/{}", jobId, processed, totalRecords);
                    }
                }
            } // end for rows

            // final progress update
            jobRepository.incrementProcessedRecords(jobId, processed % progressUpdateBatchSize, LocalDateTime.now());
            jobRepository.updateTotalRecords(jobId, totalRecords, LocalDateTime.now());
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.COMPLETED, null, LocalDateTime.now());
            long endMs = System.currentTimeMillis();
            log.info("PROCESS_COMPLETE jobId={} totalRecords={} durationMs={}", jobId, totalRecords, (endMs - startMs));

        } catch (Exception e) {
            log.error("PROCESS_FAILED jobId={} error={}", jobId, e.getMessage(), e);
            String truncated = truncate(Optional.ofNullable(e.getMessage()).orElse("Unexpected error"), 2000);
            try {
                entityManager.clear();
            } catch (Exception ex) {
                log.warn("entityManager.clear failed: {}", ex.getMessage());
            }
            markJobFailedNewTx(jobId, truncated);
        } finally {
            cleanupTempFile(tmpFile, jobId);
        }
    }

    // Helper to apply DTO into entity and overwrite all fields (blank/null override)
    private void applyCollegeCourseDtoToEntityOverwriting(CollegeCourse e, CollegeCourseRequestExcelDto dto) {
        // courseUrl
        e.setCourseUrl(NormalizationUtil.nullifyIfEmpty(dto.getCourseUrl()));
        // duration - parse to integer months; if null set to 0 (per earlier agreed default)
        Integer dur = NormalizationUtil.parseDurationMonths(dto.getDuration());
        if (dur == null) {
            // try parse numeric string
            try {
                if (dto.getDuration() != null && !dto.getDuration().isBlank()) {
                    dur = Integer.parseInt(dto.getDuration().trim());
                }
            } catch (Exception ignored) {}
        }
        e.setDuration(dur == null ? 0 : dur);

        // intake months -> convert to enum list
        List<com.consultancy.education.enums.Month> months = new ArrayList<>();
        List<com.consultancy.education.enums.Month> parsedMonths = NormalizationUtil.parseIntakeMonthsToEnum(dto.getIntakeMonths())
                .stream().map(m -> m).collect(Collectors.toList());
        if (parsedMonths != null && !parsedMonths.isEmpty()) {
            months.addAll(parsedMonths);
        }
        e.setIntakeMonths(months);

        // intake year default to 0 if missing
        Integer iy = dto.getIntakeYear();
        e.setIntakeYear(iy == null ? 0 : iy);

        // eligibility and fees (set null if empty)
        e.setEligibilityCriteria(NormalizationUtil.nullifyIfEmpty(dto.getEligibilityCriteria()));
        e.setApplicationFee(NormalizationUtil.nullifyIfEmpty(dto.getApplicationFee()));
        e.setTuitionFee(NormalizationUtil.nullifyIfEmpty(dto.getTuitionFee()));
        e.setScholarshipEligible(NormalizationUtil.nullifyIfEmpty(dto.getScholarshipEligible()));
        e.setScholarshipDetails(NormalizationUtil.nullifyIfEmpty(dto.getScholarshipDetails()));
        e.setBacklogAcceptanceRange(NormalizationUtil.nullifyIfEmpty(dto.getBacklogAcceptanceRange()));

        // numeric scores
        e.setIeltsMinScore(NormalizationUtil.parseDoubleOrNull(dto.getIeltsMinScore() == null ? null : String.valueOf(dto.getIeltsMinScore())));
        e.setIeltsMinBandScore(NormalizationUtil.parseDoubleOrNull(dto.getIeltsMinBandScore() == null ? null : String.valueOf(dto.getIeltsMinBandScore())));
        e.setToeflMinScore(NormalizationUtil.parseDoubleOrNull(dto.getToeflMinScore() == null ? null : String.valueOf(dto.getToeflMinScore())));
        e.setToeflMinBandScore(NormalizationUtil.parseDoubleOrNull(dto.getToeflMinBandScore() == null ? null : String.valueOf(dto.getToeflMinBandScore())));
        e.setPteMinScore(NormalizationUtil.parseDoubleOrNull(dto.getPteMinScore() == null ? null : String.valueOf(dto.getPteMinScore())));
        e.setPteMinBandScore(NormalizationUtil.parseDoubleOrNull(dto.getPteMinBandScore() == null ? null : String.valueOf(dto.getPteMinBandScore())));
        e.setDetMinScore(NormalizationUtil.parseDoubleOrNull(dto.getDetMinScore() == null ? null : String.valueOf(dto.getDetMinScore())));
        e.setGreMinScore(NormalizationUtil.parseDoubleOrNull(dto.getGreMinScore() == null ? null : String.valueOf(dto.getGreMinScore())));
        e.setGmatMinScore(NormalizationUtil.parseDoubleOrNull(dto.getGmatMinScore() == null ? null : String.valueOf(dto.getGmatMinScore())));
        e.setSatMinScore(NormalizationUtil.parseDoubleOrNull(dto.getSatMinScore() == null ? null : String.valueOf(dto.getSatMinScore())));

        e.setMin10thScore(dto.getMin10thScore());
        e.setMinInterScore(dto.getMinInterScore());
        e.setMinGraduationScore(dto.getMinGraduationScore());

        // append remarks (raw) - overwrite completely
        e.setRemarks(NormalizationUtil.nullifyIfEmpty(dto.getRemarks()));
    }

    private Map<String, Object> dtoToRaw(CollegeCourseRequestExcelDto dto) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("campusCode", dto.getCampusCode());
        map.put("courseName", dto.getCourseName());
        map.put("department", dto.getDepartment());
        map.put("graduationLevel", dto.getGraduationLevel());
        map.put("duration", dto.getDuration());
        map.put("intakeMonths", dto.getIntakeMonths());
        // add more if needed
        return map;
    }

    private void persistError(String fileName, Integer rowNumber, String entityType, String identifier, Object rawData, String errorMessage) {
        try {
            BulkUploadError e = BulkUploadError.builder()
                    .fileName(fileName)
                    .rowNumber(rowNumber)
                    .entityType(entityType)
                    .identifier(identifier)
                    .rawData(rawData == null ? null : rawData.toString())
                    .errorMessage(errorMessage == null ? "" : errorMessage)
                    .build();
            bulkUploadErrorRepository.save(e);
        } catch (Exception ex) {
            log.error("Failed to persist bulk_upload_error: {}", ex.getMessage(), ex);
        }
    }

    private void maybeUpdateProgress(Long jobId, int delta) {
        jobRepository.incrementProcessedRecords(jobId, delta, LocalDateTime.now());
    }

    @Transactional
    protected void batchInsertCollegeCourses(List<CollegeCourse> list) {
        if (list == null || list.isEmpty()) return;
        int i = 0;
        for (CollegeCourse cc : list) {
            entityManager.persist(cc);
            i++;
            if (i % persistBatchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    private File saveToTempFile(MultipartFile file, Long jobId) throws Exception {
        String safeName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.xlsx").replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        File tmp = Files.createTempFile("bulkjob-" + jobId + "-", "-" + safeName).toFile();
        try (InputStream in = file.getInputStream(); FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
        }
        tmp.deleteOnExit();
        return tmp;
    }

    private void cleanupTempFile(File f, Long jobId) {
        if (f == null) return;
        try {
            boolean deleted = f.delete();
            log.debug("CLEANUP_TMP jobId={} tmpPath={} deleted={}", jobId, f.getAbsolutePath(), deleted);
        } catch (Exception ex) {
            log.warn("CLEANUP_TMP_FAILED jobId={} tmpPath={} error={}", jobId, f.getAbsolutePath(), ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markJobFailedNewTx(Long jobId, String truncatedMsg) {
        try {
            jobRepository.updateStatusAndError(jobId, BulkUploadJob.Status.FAILED, truncatedMsg, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to update job FAILED in new tx for job {} : {}", jobId, e.getMessage(), e);
        }
    }

    private String makeCourseKey(String name, String dept, String level) {
        return (name == null ? "" : name.trim()) + "|" + (dept == null ? "" : dept.trim()) + "|" + (level == null ? "" : level.trim());
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

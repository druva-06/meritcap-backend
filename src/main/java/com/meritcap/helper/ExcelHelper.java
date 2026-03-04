package com.meritcap.helper;

import com.meritcap.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.meritcap.DTOs.requestDTOs.course.CourseRequestDto;
import com.meritcap.exception.ExcelException;
import com.meritcap.model.College;
import com.meritcap.utils.BasicValidations;
import com.meritcap.utils.FormatConverter;
import com.meritcap.utils.NormalizationUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Robust ExcelHelper that parses Excel workbooks in a single pass.
 * Handles both "colleges" sheet format (rich, with campus codes)
 * and "Sheet1" format (lighter, may lack campus codes).
 *
 * Key features:
 * - Parse each sheet ONCE and extract College, Course, CollegeCourse data
 * together
 * - Header aliasing tolerates different column names, typos, extra whitespace
 * - Per-row error collection with row number + field details + data snippet
 * - Never throws on bad rows; skips and records error
 * - Backward-compatible static methods still available
 */
public final class ExcelHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelHelper.class);
    private static final BasicValidations validations = new BasicValidations();

    private ExcelHelper() {
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public static boolean checkExcelFormat(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType == null)
            return false;
        return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("application/vnd.ms-excel");
    }

    /**
     * Single-pass parse result containing all three entity types extracted from one
     * sheet.
     */
    public static class CombinedParseResult {
        private List<College> colleges = new ArrayList<>();
        private List<CourseRequestDto> courses = new ArrayList<>();
        private List<CollegeCourseRequestExcelDto> collegeCourses = new ArrayList<>();
        private List<RowError> rowErrors = new ArrayList<>();
        private String sheetName;
        private int totalDataRows;

        public List<College> getColleges() {
            return colleges;
        }

        public List<CourseRequestDto> getCourses() {
            return courses;
        }

        public List<CollegeCourseRequestExcelDto> getCollegeCourses() {
            return collegeCourses;
        }

        public List<RowError> getRowErrors() {
            return rowErrors;
        }

        public String getSheetName() {
            return sheetName;
        }

        public int getTotalDataRows() {
            return totalDataRows;
        }
    }

    /**
     * Parse the entire workbook. Tries "colleges" sheet first, then "Sheet1", then
     * first sheet.
     */
    public static CombinedParseResult parseWorkbook(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet("colleges");
            String sheetName = "colleges";

            if (sheet == null) {
                sheet = workbook.getSheet("Sheet1");
                sheetName = "Sheet1";
            }
            if (sheet == null && workbook.getNumberOfSheets() > 0) {
                sheet = workbook.getSheetAt(0);
                sheetName = sheet.getSheetName();
            }
            if (sheet == null) {
                throw new ExcelException("No data sheet found in workbook");
            }

            log.info("parseWorkbook - using sheet: '{}', lastRow={}", sheetName, sheet.getLastRowNum());
            return parseSheet(sheet, sheetName);

        } catch (ExcelException ee) {
            throw ee;
        } catch (Exception e) {
            log.error("Failed to parse workbook: {}", e.getMessage(), e);
            throw new ExcelException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    /**
     * Parse from a File instead of InputStream (avoids re-reading).
     */
    public static CombinedParseResult parseWorkbook(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return parseWorkbook(fis);
        }
    }

    // =========================================================================
    // Internal sheet parser
    // =========================================================================

    private static CombinedParseResult parseSheet(Sheet sheet, String sheetName) {
        CombinedParseResult result = new CombinedParseResult();
        result.sheetName = sheetName;

        Iterator<Row> iterator = sheet.iterator();
        if (!iterator.hasNext()) {
            result.getRowErrors().add(new RowError(1, "Sheet '" + sheetName + "' is empty", null));
            return result;
        }

        // Build header map from first row
        Row headerRow = iterator.next();
        Map<String, Integer> headerMap = buildHeaderMap(headerRow);
        log.info("parseSheet '{}' - detected {} canonical mappings from headers", sheetName, headerMap.size());

        // Validate minimum required headers
        List<String> missingRequired = new ArrayList<>();
        if (!headerMap.containsKey("university name"))
            missingRequired.add("University / University Name");
        if (!headerMap.containsKey("program name"))
            missingRequired.add("Program Name / Name / Course Name");
        if (headerMap.isEmpty())
            missingRequired.add("(no headers detected at all)");

        if (!missingRequired.isEmpty()) {
            result.getRowErrors().add(new RowError(1,
                    "Missing required headers: " + String.join(", ", missingRequired) +
                            ". Found headers: " + getRawHeaders(headerRow),
                    null));
            return result;
        }

        // Track unique colleges and courses for dedup
        Map<String, College> collegeMap = new LinkedHashMap<>(); // campusCode -> College
        Map<String, CourseRequestDto> courseMap = new LinkedHashMap<>(); // name|dept|level -> CourseRequestDto

        int dataRowCount = 0;
        while (iterator.hasNext()) {
            Row row = iterator.next();
            int rowNum = row.getRowNum() + 1; // 1-based for user display

            if (isRowEmpty(row))
                continue;
            dataRowCount++;

            try {
                List<String> rowProblems = new ArrayList<>();

                // Required fields
                String universityName = safeString(row, headerMap, "university name");
                String campusCode = safeString(row, headerMap, "campus code");
                String programName = safeString(row, headerMap, "program name");
                String studyLevel = safeString(row, headerMap, "study level");
                String campus = safeString(row, headerMap, "campus");

                if (isBlank(universityName))
                    rowProblems.add("University name is required");
                if (isBlank(programName))
                    rowProblems.add("Program name / Course name is required");
                if (isBlank(studyLevel))
                    rowProblems.add("Study level / Graduation level is required");

                // Campus code: auto-generate if missing
                if (isBlank(campusCode)) {
                    if (!isBlank(universityName)) {
                        campusCode = NormalizationUtil.generateCampusCode(universityName, campus);
                    } else {
                        rowProblems.add("Campus code is missing and cannot be auto-generated without university name");
                    }
                } else {
                    campusCode = campusCode.trim().toUpperCase();
                }

                if (!rowProblems.isEmpty()) {
                    result.getRowErrors().add(new RowError(rowNum,
                            "Row " + rowNum + ": " + String.join("; ", rowProblems),
                            cellRowSnippet(row)));
                    continue;
                }

                // ---------- Build College (dedup by campusCode) ----------
                if (!collegeMap.containsKey(campusCode)) {
                    College college = new College();
                    college.setName(universityName.trim());
                    college.setCampusName(isBlank(campus) ? null : campus.trim());
                    college.setCampusCode(campusCode);
                    college.setWebsiteUrl(safeString(row, headerMap, "website url"));
                    college.setCollegeLogo(safeString(row, headerMap, "college logo"));
                    college.setCountry(safeString(row, headerMap, "country"));
                    college.setEstablishedYear(safeInteger(row, headerMap, "established year"));
                    college.setRanking(safeString(row, headerMap, "ranking"));
                    college.setDescription(safeString(row, headerMap, "description"));
                    college.setCampusGalleryVideoLink(safeString(row, headerMap, "campus gallery video link"));
                    college.setFaqsUniversity(safeString(row, headerMap, "faqs university"));
                    collegeMap.put(campusCode, college);
                } else {
                    // Update FAQs if current row has it and existing doesn't
                    College existing = collegeMap.get(campusCode);
                    String faqsUni = safeString(row, headerMap, "faqs university");
                    if (!isBlank(faqsUni) && isBlank(existing.getFaqsUniversity())) {
                        existing.setFaqsUniversity(faqsUni);
                    }
                }

                // ---------- Build Course (dedup by name|dept|level) ----------
                String normalizedLevel = NormalizationUtil.normalizeGraduationLevel(studyLevel);
                if (isBlank(normalizedLevel))
                    normalizedLevel = studyLevel.trim().toUpperCase();
                String department = safeString(row, headerMap, "department");
                String specialization = safeString(row, headerMap, "specialization");
                String courseKey = makeCourseKey(programName, department, normalizedLevel);

                if (!courseMap.containsKey(courseKey)) {
                    CourseRequestDto courseDto = new CourseRequestDto();
                    courseDto.setName(programName.trim());
                    courseDto.setDepartment(department);
                    courseDto.setSpecialization(specialization);
                    courseDto.setGraduationLevel(normalizedLevel);
                    courseMap.put(courseKey, courseDto);
                }

                // ---------- Build CollegeCourse (one per row) ----------
                CollegeCourseRequestExcelDto cc = new CollegeCourseRequestExcelDto();
                cc.setCampusCode(campusCode);
                cc.setCourseName(programName.trim());
                cc.setDepartment(department);
                cc.setGraduationLevel(normalizedLevel);
                cc.setCourseUrl(safeString(row, headerMap, "course url"));

                // Duration
                String durationRaw = safeString(row, headerMap, "duration");
                if (!isBlank(durationRaw)) {
                    try {
                        Integer months = FormatConverter.cnvrtDurationToInteger(durationRaw);
                        cc.setDuration(months != null ? months.toString() : durationRaw);
                    } catch (Exception ex) {
                        cc.setDuration(durationRaw);
                    }
                }

                // Intake
                cc.setIntakeMonths(safeString(row, headerMap, "intake month"));
                cc.setIntakeYear(safeInteger(row, headerMap, "intake year"));

                // Requirements & fees
                cc.setEligibilityCriteria(safeString(row, headerMap, "eligibility criteria"));
                cc.setApplicationFee(safeString(row, headerMap, "application fee"));

                Double tuitionNum = safeDouble(row, headerMap, "yearly tuition fees");
                if (tuitionNum != null) {
                    cc.setTuitionFee(String.valueOf(tuitionNum));
                } else {
                    cc.setTuitionFee(safeString(row, headerMap, "yearly tuition fees"));
                }

                // Test scores
                cc.setIeltsMinScore(safeDouble(row, headerMap, "ielts score"));
                cc.setIeltsMinBandScore(safeDouble(row, headerMap, "ielts no band less than"));
                cc.setToeflMinScore(safeDouble(row, headerMap, "toefl score"));
                cc.setToeflMinBandScore(safeDouble(row, headerMap, "toefl no band less than"));
                cc.setPteMinScore(safeDouble(row, headerMap, "pte score"));
                cc.setPteMinBandScore(safeDouble(row, headerMap, "pte no band less than"));
                cc.setDetMinScore(safeDouble(row, headerMap, "det score"));
                cc.setGreMinScore(safeDouble(row, headerMap, "gre score"));
                cc.setGmatMinScore(safeDouble(row, headerMap, "gmat score"));
                cc.setSatMinScore(safeDouble(row, headerMap, "sat score"));
                cc.setCatMinScore(safeDouble(row, headerMap, "cat score"));

                // Academic scores
                cc.setMin10thScore(safeDouble(row, headerMap, "10th"));
                cc.setMinInterScore(safeDouble(row, headerMap, "inter"));
                cc.setMinGraduationScore(safeDouble(row, headerMap, "graduation score"));

                // Scholarship & backlog
                cc.setScholarshipEligible(safeString(row, headerMap, "scholarship available"));
                cc.setScholarshipDetails(safeString(row, headerMap, "scholarship detail"));
                cc.setBacklogAcceptanceRange(safeString(row, headerMap, "backlog range"));
                cc.setRemarks(safeString(row, headerMap, "remarks"));

                // Rich content fields (AN-AZ)
                cc.setCredits(safeString(row, headerMap, "credits"));
                cc.setDetailedScholarshipInfo(safeString(row, headerMap, "scholarship details extra"));
                cc.setWhyChooseThisCourse(safeString(row, headerMap, "why choose this course"));
                cc.setAboutCourse(safeString(row, headerMap, "about course"));
                cc.setKeyFeatures(safeString(row, headerMap, "key features"));
                cc.setLearningOutcomes(safeString(row, headerMap, "learning outcomes"));
                cc.setCourseHighlights(safeString(row, headerMap, "course highlights"));
                cc.setCareerOpportunity(safeString(row, headerMap, "career opportunity"));
                cc.setFaqsCourse(safeString(row, headerMap, "faqs courses"));
                cc.setFaqsUniversity(safeString(row, headerMap, "faqs university"));
                cc.setCoreModules(safeString(row, headerMap, "core modules"));
                cc.setAssessmentMethods(safeString(row, headerMap, "assessment methods"));
                cc.setJobMarkets(safeString(row, headerMap, "job markets"));

                result.getCollegeCourses().add(cc);

            } catch (Exception ex) {
                log.warn("Row {} parse error: {}", rowNum, ex.getMessage());
                result.getRowErrors().add(new RowError(rowNum,
                        "Row " + rowNum + ": Unexpected error - " + ex.getMessage(),
                        cellRowSnippet(row)));
            }
        }

        result.totalDataRows = dataRowCount;
        result.colleges.addAll(collegeMap.values());
        result.courses.addAll(courseMap.values());

        log.info("parseSheet '{}' complete: dataRows={}, colleges={}, courses={}, collegeCourses={}, errors={}",
                sheetName, dataRowCount, result.colleges.size(), result.courses.size(),
                result.collegeCourses.size(), result.rowErrors.size());

        return result;
    }

    // =========================================================================
    // Header aliasing — maps many possible Excel header names to canonical keys
    // =========================================================================

    private static final Map<String, List<String>> HEADER_ALIASES = new LinkedHashMap<>();
    static {
        // The order matters — aliases are matched first-come-first-served.
        // More specific aliases come BEFORE the generic "name" alias.

        // College fields
        HEADER_ALIASES.put("university name", Arrays.asList(
                "university", "university name", "college name", "institution name", "institution"));
        HEADER_ALIASES.put("campus", Arrays.asList("campus", "campus name"));
        HEADER_ALIASES.put("campus code", Arrays.asList("campus code", "campuscode", "college code"));
        HEADER_ALIASES.put("website url", Arrays.asList(
                "webiste url", "website url", "website", "college website", "university website"));
        HEADER_ALIASES.put("college logo", Arrays.asList("college logo", "logo", "university logo"));
        HEADER_ALIASES.put("country", Arrays.asList("country"));
        HEADER_ALIASES.put("established year", Arrays.asList(
                "established year", "established", "year established", "founded year"));
        HEADER_ALIASES.put("ranking", Arrays.asList("ranking", "university ranking", "rank"));
        HEADER_ALIASES.put("description", Arrays.asList(
                "description", "about university", "university description", "about"));
        HEADER_ALIASES.put("campus gallery video link", Arrays.asList(
                "campus gallery video link", "campus gallery vedio link", "gallery video", "video link"));

        // Course fields
        HEADER_ALIASES.put("program name", Arrays.asList(
                "program name", "programme name", "course name", "course", "name"));
        HEADER_ALIASES.put("specialization", Arrays.asList("specialization", "specialisation"));
        HEADER_ALIASES.put("department", Arrays.asList("department", "dept"));
        HEADER_ALIASES.put("study level", Arrays.asList(
                "study level", "graduation level", "level", "degree level", "degree type"));

        // CollegeCourse fields
        HEADER_ALIASES.put("course url", Arrays.asList("course url", "courseurl", "program url"));
        HEADER_ALIASES.put("duration", Arrays.asList("duration", "course duration"));
        HEADER_ALIASES.put("intake month", Arrays.asList(
                "open intakes", "intake month", "intake months", "intakes"));
        HEADER_ALIASES.put("intake year", Arrays.asList("intake year"));
        HEADER_ALIASES.put("eligibility criteria", Arrays.asList(
                "entry requirements", "eligibility criteria", "eligibility", "requirements", "entry requirement"));
        HEADER_ALIASES.put("application fee", Arrays.asList("application fee", "app fee"));
        HEADER_ALIASES.put("yearly tuition fees", Arrays.asList(
                "yearly tuition fees", "tuition fee", "tuition fees", "yearly tuition", "annual tuition"));

        // Test scores
        HEADER_ALIASES.put("ielts score", Arrays.asList("ielts score", "ielts"));
        HEADER_ALIASES.put("ielts no band less than", Arrays.asList(
                "ielts no band less than", "ielts band", "ielts minimum band"));
        HEADER_ALIASES.put("toefl score", Arrays.asList("toefl score", "tofel score", "toefl"));
        HEADER_ALIASES.put("toefl no band less than", Arrays.asList(
                "toefl no band less than", "tofel no band less than", "toefl band"));
        HEADER_ALIASES.put("pte score", Arrays.asList("pte score", "pte"));
        HEADER_ALIASES.put("pte no band less than", Arrays.asList(
                "pte no band less than", "pte band"));
        HEADER_ALIASES.put("det score", Arrays.asList("det score", "det"));
        HEADER_ALIASES.put("gre score", Arrays.asList("gre score", "gre"));
        HEADER_ALIASES.put("gmat score", Arrays.asList("gmat score", "gmat"));
        HEADER_ALIASES.put("sat score", Arrays.asList("sat score", "sat"));
        HEADER_ALIASES.put("cat score", Arrays.asList("cat score", "cat"));

        // Academic scores
        HEADER_ALIASES.put("10th", Arrays.asList("10th", "10 th", "10th score", "ssc", "class 10"));
        HEADER_ALIASES.put("inter", Arrays.asList("inter", "12th", "12 th", "intermediate", "hsc", "class 12"));
        HEADER_ALIASES.put("graduation score", Arrays.asList(
                "graduation", "graduation score", "ug score", "bachelor score", "degree score"));

        // Scholarship & misc
        HEADER_ALIASES.put("scholarship available", Arrays.asList(
                "scholarship available", "scholarship", "scholarship eligible"));
        HEADER_ALIASES.put("scholarship detail", Arrays.asList("scholarship detail"));
        HEADER_ALIASES.put("backlog range", Arrays.asList(
                "backlog range", "backlogrange", "backlogs", "backlog"));
        HEADER_ALIASES.put("remarks", Arrays.asList("remarks", "notes", "comments"));

        // Rich content fields (columns AN-AZ in the "colleges" sheet)
        HEADER_ALIASES.put("credits", Arrays.asList("credits", "course credits", "total credits"));
        HEADER_ALIASES.put("scholarship details extra", Arrays.asList("scholarship details"));
        HEADER_ALIASES.put("why choose this course", Arrays.asList(
                "why chooses this course?", "why chooses this course",
                "why choose this course?", "why choose this course", "why this course"));
        HEADER_ALIASES.put("about course", Arrays.asList(
                "about course", "about the course", "course description", "course overview"));
        HEADER_ALIASES.put("key features", Arrays.asList(
                "key features (course)", "key features", "course features"));
        HEADER_ALIASES.put("learning outcomes", Arrays.asList(
                "learning outcomes  (course)", "learning outcomes (course)",
                "learning outcomes", "course outcomes"));
        HEADER_ALIASES.put("course highlights", Arrays.asList("course highlights", "highlights"));
        HEADER_ALIASES.put("career opportunity", Arrays.asList(
                "career oppurtunity", "career opportunity", "career opportunities"));
        HEADER_ALIASES.put("faqs courses", Arrays.asList(
                "faqs (courses)", "faqs courses", "course faqs", "faq course"));
        HEADER_ALIASES.put("faqs university", Arrays.asList(
                "faqs (university)", "faqs university", "university faqs", "faq university"));
        HEADER_ALIASES.put("core modules", Arrays.asList(
                "core modules", "modules", "course modules"));
        HEADER_ALIASES.put("assessment methods", Arrays.asList(
                "assesment & learning methods", "assessment & learning methods",
                "assessment methods", "learning methods"));
        HEADER_ALIASES.put("job markets", Arrays.asList(
                "job markets", "job market", "employment sectors"));
    }

    /**
     * Build canonical-key -> column-index mapping from the header row.
     * Handles the "name" ambiguity (university vs program) by:
     * 1) university name aliases are checked first (they include "university" which
     * is specific)
     * 2) "name" only matches program name if university was already matched by
     * "university"
     */
    private static Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> canonical = new LinkedHashMap<>();
        if (headerRow == null)
            return canonical;

        // Collect all raw header -> column index (normalized)
        Map<String, Integer> normalizedToIndex = new LinkedHashMap<>();
        for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
            Cell c = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c == null)
                continue;
            String raw = cellToString(c);
            if (raw == null)
                continue;
            String norm = normalize(raw);
            if (!norm.isEmpty()) {
                // Keep first occurrence of each normalized header
                normalizedToIndex.putIfAbsent(norm, i);
            }
        }

        // Match canonical keys to header columns via aliases
        Set<Integer> claimedColumns = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : HEADER_ALIASES.entrySet()) {
            String canonicalKey = entry.getKey();
            for (String alias : entry.getValue()) {
                String normAlias = normalize(alias);
                Integer colIdx = normalizedToIndex.get(normAlias);
                if (colIdx != null && !claimedColumns.contains(colIdx)) {
                    canonical.put(canonicalKey, colIdx);
                    claimedColumns.add(colIdx);
                    break;
                }
            }
        }

        // Handle special case: there may be two "Scholarship Detail(s)" columns
        // (e.g., AK="Scholarship Detail" and AO="Scholarship Details ")
        // If "scholarship details extra" wasn't matched, look for it
        if (!canonical.containsKey("scholarship details extra")) {
            for (Map.Entry<String, Integer> norm : normalizedToIndex.entrySet()) {
                if (norm.getKey().startsWith("scholarship detail")
                        && !claimedColumns.contains(norm.getValue())) {
                    canonical.put("scholarship details extra", norm.getValue());
                    claimedColumns.add(norm.getValue());
                    break;
                }
            }
        }

        log.debug("buildHeaderMap - {}", canonical);
        return canonical;
    }

    // =========================================================================
    // Cell helpers
    // =========================================================================

    private static String normalize(String s) {
        if (s == null)
            return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static Cell getCell(Row row, Map<String, Integer> headerMap, String canonicalKey) {
        if (row == null || headerMap == null)
            return null;
        Integer idx = headerMap.get(canonicalKey);
        if (idx == null)
            return null;
        return row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    private static String safeString(Row row, Map<String, Integer> headerMap, String key) {
        try {
            return validations.validateString(getCell(row, headerMap, key));
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer safeInteger(Row row, Map<String, Integer> headerMap, String key) {
        try {
            return validations.validateInteger(getCell(row, headerMap, key));
        } catch (Exception e) {
            return null;
        }
    }

    private static Double safeDouble(Row row, Map<String, Integer> headerMap, String key) {
        try {
            return validations.validateDouble(getCell(row, headerMap, key));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null) {
                String val = cellToString(c);
                if (val != null && !val.trim().isEmpty())
                    return false;
            }
        }
        return true;
    }

    static String cellToString(Cell cell) {
        if (cell == null)
            return null;
        try {
            CellType t = cell.getCellType();
            switch (t) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        try {
                            return cell.getLocalDateTimeCellValue().toString();
                        } catch (Exception ex) {
                            return String.valueOf(cell.getDateCellValue());
                        }
                    }
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d) && !Double.isInfinite(d))
                        return String.valueOf((long) d);
                    return String.valueOf(d);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ignored) {
                        try {
                            double dv = cell.getNumericCellValue();
                            if (dv == Math.floor(dv))
                                return String.valueOf((long) dv);
                            return String.valueOf(dv);
                        } catch (Exception inner) {
                            return cell.getCellFormula();
                        }
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static String cellRowSnippet(Row row) {
        if (row == null)
            return null;
        StringBuilder sb = new StringBuilder();
        int last = Math.min(8, row.getLastCellNum());
        if (last < 0)
            last = 0;
        for (int i = 0; i < last; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String s = cellToString(c);
            if (s != null) {
                if (sb.length() > 0)
                    sb.append(" | ");
                String trimmed = s.replaceAll("\\s+", " ").trim();
                if (trimmed.length() > 50)
                    trimmed = trimmed.substring(0, 50) + "...";
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }

    private static String getRawHeaders(Row headerRow) {
        if (headerRow == null)
            return "none";
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell c = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String val = cellToString(c);
            if (val != null && !val.trim().isEmpty())
                headers.add(val.trim());
        }
        return headers.toString();
    }

    private static String makeCourseKey(String name, String dept, String level) {
        return (name == null ? "" : name.trim().toLowerCase()) + "|" +
                (dept == null ? "" : dept.trim().toLowerCase()) + "|" +
                (level == null ? "" : level.trim().toLowerCase());
    }

    // =========================================================================
    // RowError class
    // =========================================================================

    public static class RowError {
        private final int rowNumber;
        private final String message;
        private final String snippet;

        public RowError(int rowNumber, String message, String snippet) {
            this.rowNumber = rowNumber;
            this.message = message;
            this.snippet = snippet;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getMessage() {
            return message;
        }

        public String getSnippet() {
            return snippet;
        }

        @Override
        public String toString() {
            return "RowError{row=" + rowNumber + ", msg='" + message + "'" +
                    (snippet != null ? ", data='" + snippet + "'" : "") + "}";
        }
    }

    // =========================================================================
    // Backward-compatible wrapper (ParseResult)
    // =========================================================================

    public static class ParseResult<T> {
        private List<T> items = new ArrayList<>();
        private List<RowError> rowErrors = new ArrayList<>();

        public ParseResult() {
        }

        public ParseResult(List<T> items, List<RowError> rowErrors) {
            this.items = items;
            this.rowErrors = rowErrors;
        }

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }

        public List<RowError> getRowErrors() {
            return rowErrors;
        }

        public void setRowErrors(List<RowError> rowErrors) {
            this.rowErrors = rowErrors;
        }
    }

    public static ParseResult<College> parseColleges(InputStream inputStream) throws Exception {
        CombinedParseResult combined = parseWorkbook(inputStream);
        return new ParseResult<>(combined.getColleges(), combined.getRowErrors());
    }

    public static ParseResult<CourseRequestDto> parseCourses(InputStream inputStream) throws Exception {
        CombinedParseResult combined = parseWorkbook(inputStream);
        return new ParseResult<>(combined.getCourses(), combined.getRowErrors());
    }

    public static ParseResult<CollegeCourseRequestExcelDto> parseCollegeCourses(InputStream inputStream)
            throws Exception {
        CombinedParseResult combined = parseWorkbook(inputStream);
        return new ParseResult<>(combined.getCollegeCourses(), combined.getRowErrors());
    }

    public static List<College> convertCollegeExcelIntoList(InputStream inputStream) throws Exception {
        return parseColleges(inputStream).getItems();
    }

    public static List<CourseRequestDto> convertCourseExcelIntoList(InputStream inputStream) throws Exception {
        return parseCourses(inputStream).getItems();
    }

    public static List<CollegeCourseRequestExcelDto> convertCollegeCourseExcelIntoList(InputStream inputStream)
            throws Exception {
        return parseCollegeCourses(inputStream).getItems();
    }
}

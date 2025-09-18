package com.consultancy.education.helper;

import com.consultancy.education.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.consultancy.education.DTOs.requestDTOs.course.CourseRequestDto;
import com.consultancy.education.exception.ExcelException;
import com.consultancy.education.model.College;
import com.consultancy.education.utils.BasicValidations;
import com.consultancy.education.utils.FormatConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Robust ExcelHelper with header aliasing and ParseResult support.
 * Backwards-compatible methods:
 *  - convertCollegeExcelIntoList(InputStream)
 *  - convertCourseExcelIntoList(InputStream)
 *  - convertCollegeCourseExcelIntoList(InputStream)
 */
public final class ExcelHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelHelper.class);
    private static final BasicValidations validations = new BasicValidations();

    private ExcelHelper() {}

    // -------------------------
    // Public compatibility methods
    // -------------------------
    public static boolean checkExcelFormat(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType == null) return false;
        return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("application/vnd.ms-excel"); // be lenient
    }

    // -------------------------------------------------------
    // Small helper classes used to return parsing details
    // -------------------------------------------------------
    public static class RowError {
        private final int rowNumber;
        private final String message;
        private final String snippet;

        public RowError(int rowNumber, String message, String snippet) {
            this.rowNumber = rowNumber;
            this.message = message;
            this.snippet = snippet;
        }
        public int getRowNumber() { return rowNumber; }
        public String getMessage() { return message; }
        public String getSnippet() { return snippet; }
        @Override public String toString() { return "RowError{" + "row=" + rowNumber + ", msg='" + message + '\'' + ", snippet='" + snippet + '\'' + '}'; }
    }

    public static class ParseResult<T> {
        private List<T> items = new ArrayList<>();
        private List<RowError> rowErrors = new ArrayList<>();

        public ParseResult() {}
        public ParseResult(List<T> items, List<RowError> rowErrors) {
            this.items = items;
            this.rowErrors = rowErrors;
        }
        public List<T> getItems() { return items; }
        public void setItems(List<T> items) { this.items = items; }
        public List<RowError> getRowErrors() { return rowErrors; }
        public void setRowErrors(List<RowError> rowErrors) { this.rowErrors = rowErrors; }
    }

    // -------------------------
    // Header normalization & aliases
    // -------------------------
    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * Canonical -> aliases (normalized). Add aliases as needed for different Excel variations.
     * Important: include "study level" as alias for "graduation level".
     */
    private static final Map<String, List<String>> HEADER_ALIASES = new LinkedHashMap<>();
    static {
        HEADER_ALIASES.put("university name", Arrays.asList("university name", "name", "university"));
        HEADER_ALIASES.put("campus", Arrays.asList("campus"));
        HEADER_ALIASES.put("campus code", Arrays.asList("campus code", "campuscode"));
        HEADER_ALIASES.put("website url", Arrays.asList("website url", "website", "website url2", "website url 2"));
        HEADER_ALIASES.put("college logo", Arrays.asList("college logo", "collegelogo"));
        HEADER_ALIASES.put("country", Arrays.asList("country"));
        HEADER_ALIASES.put("established year", Arrays.asList("established year", "established"));
        HEADER_ALIASES.put("ranking", Arrays.asList("ranking"));
        HEADER_ALIASES.put("description", Arrays.asList("description"));
        HEADER_ALIASES.put("campus gallery video link", Arrays.asList("campus gallery video link", "campus gallery video", "campus gallery vedio link"));
        HEADER_ALIASES.put("eligibility criteria", Arrays.asList("eligibility criteria", "eligibility"));
        HEADER_ALIASES.put("course name", Arrays.asList("course name", "course"));
        HEADER_ALIASES.put("specialization", Arrays.asList("specialization"));
        HEADER_ALIASES.put("department", Arrays.asList("department"));
        HEADER_ALIASES.put("study level", Arrays.asList("study level")); // keep separate for clarity
        HEADER_ALIASES.put("graduation level", Arrays.asList("graduation level", "graduation", "study level")); // make study level an alias for graduation level
        HEADER_ALIASES.put("website url2", Arrays.asList("website url2", "website url 2"));
        HEADER_ALIASES.put("course url", Arrays.asList("course url", "courseurl", "website url"));
        HEADER_ALIASES.put("duration", Arrays.asList("duration"));
        HEADER_ALIASES.put("intake month", Arrays.asList("intake month", "intake months"));
        HEADER_ALIASES.put("intake year", Arrays.asList("intake year"));
        HEADER_ALIASES.put("entry requirements", Arrays.asList("entry requirements"));
        HEADER_ALIASES.put("application fee", Arrays.asList("application fee", "applicationfee"));
        HEADER_ALIASES.put("yearly tuition fees", Arrays.asList("yearly tuition fees", "tuition fee", "tuition fees", "yearly tuition"));
        HEADER_ALIASES.put("ielts score", Arrays.asList("ielts score", "ielts"));
        HEADER_ALIASES.put("ielts no band less than", Arrays.asList("ielts no band less than", "ielts no band less than"));
        HEADER_ALIASES.put("pte score", Arrays.asList("pte score", "pte"));
        HEADER_ALIASES.put("pte no band less than", Arrays.asList("pte no band less than"));
        HEADER_ALIASES.put("tofel score", Arrays.asList("tofel score", "tofel", "toefl score", "toefl"));
        HEADER_ALIASES.put("tofel band", Arrays.asList("tofel band", "toefl band"));
        HEADER_ALIASES.put("det", Arrays.asList("det"));
        HEADER_ALIASES.put("gre score", Arrays.asList("gre score", "gre"));
        HEADER_ALIASES.put("gmat score", Arrays.asList("gmat score", "gmat"));
        HEADER_ALIASES.put("sat score", Arrays.asList("sat score", "sat"));
        HEADER_ALIASES.put("act score", Arrays.asList("act score", "act"));
        HEADER_ALIASES.put("10th", Arrays.asList("10th", "10 th"));
        HEADER_ALIASES.put("inter", Arrays.asList("inter"));
        HEADER_ALIASES.put("graduation", Arrays.asList("graduation"));
        HEADER_ALIASES.put("scholarship", Arrays.asList("scholarship"));
        HEADER_ALIASES.put("scholarship details", Arrays.asList("scholarshipdetails", "scholarship details"));
        HEADER_ALIASES.put("backlog range", Arrays.asList("backlog range", "backlogrange"));
        HEADER_ALIASES.put("remarks", Arrays.asList("remarks"));
        HEADER_ALIASES.put("applicationmode", Arrays.asList("applicationmode", "application mode"));
        // add further aliases if you encounter new header variants
    }

    /**
     * Build a canonical mapping: canonicalKey -> columnIndex
     */
    private static Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> canonical = new LinkedHashMap<>();
        if (headerRow == null) return canonical;

        // normalized raw header -> index
        Map<String, Integer> normalizedToIndex = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell c = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c == null) continue;
            String raw = null;
            try {
                raw = c.getCellType() == CellType.STRING ? c.getStringCellValue() : null;
            } catch (Exception ex) {
                // ignore
            }
            if (raw == null) continue;
            String norm = normalize(raw);
            if (!norm.isEmpty()) normalizedToIndex.put(norm, i);
        }

        // match canonical keys by alias
        for (Map.Entry<String, List<String>> e : HEADER_ALIASES.entrySet()) {
            String canonicalKey = e.getKey();
            for (String alias : e.getValue()) {
                String normAlias = normalize(alias);
                if (normalizedToIndex.containsKey(normAlias)) {
                    canonical.put(canonicalKey, normalizedToIndex.get(normAlias));
                    break;
                }
            }
        }

        // Log for diagnostics
        log.debug("buildHeaderMap - presentHeaders={} mappedKeys={}", normalizedToIndex.keySet(), canonical.keySet());
        return canonical;
    }

    // -------------------------
    // Low-level cell helpers
    // -------------------------
    private static Cell getCell(Row row, Map<String,Integer> headerMap, String canonicalKey) {
        if (row == null || headerMap == null) return null;
        Integer idx = headerMap.get(canonicalKey);
        if (idx == null) return null;
        return row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return null;
        try {
            CellType t = cell.getCellType();
            switch (t) {
                case STRING: return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        try { return cell.getLocalDateTimeCellValue().toString(); } catch (Exception ex) { return String.valueOf(cell.getDateCellValue()); }
                    }
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) return String.valueOf((long)d);
                    return String.valueOf(d);
                case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try { return cell.getStringCellValue(); }
                    catch (Exception ignored) {
                        try {
                            double dv = cell.getNumericCellValue();
                            if (dv == Math.floor(dv)) return String.valueOf((long) dv);
                            return String.valueOf(dv);
                        } catch (Exception inner) {
                            return cell.getCellFormula();
                        }
                    }
                default: return null;
            }
        } catch (Exception e) {
            log.debug("cellToString fail", e);
            return null;
        }
    }

    private static String cellRowSnippet(Row row) {
        if (row == null) return null;
        StringBuilder sb = new StringBuilder();
        int last = Math.min(12, row.getLastCellNum());
        if (last < 0) last = 0;
        for (int i = 0; i < last; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String s = cellToString(c);
            if (s != null) {
                if (sb.length() > 0) sb.append("|");
                sb.append(s.replaceAll("\\s+", " ").trim());
            }
        }
        return sb.toString();
    }

    // -------------------------
    // parseColleges (rich version)
    // -------------------------
    public static ParseResult<College> parseColleges(InputStream inputStream) throws Exception {
        ParseResult<College> result = new ParseResult<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet("colleges");
            if (sheet == null) throw new ExcelException("Missing 'colleges' sheet");

            Iterator<Row> iterator = sheet.iterator();
            if (!iterator.hasNext()) throw new ExcelException("'colleges' sheet is empty");

            Row headerRow = iterator.next();
            Map<String,Integer> headerMap = buildHeaderMap(headerRow);
            log.info("parseColleges - detected headers: {}", headerMap.keySet());

            // log small preview of next rows
            for (int r = headerRow.getRowNum() + 1; r <= headerRow.getRowNum() + 3 && r <= sheet.getLastRowNum(); r++) {
                Row preview = sheet.getRow(r);
                if (preview != null) log.debug("parseColleges preview row {} => {}", r + 1, cellRowSnippet(preview));
            }

            int rowNum = headerRow.getRowNum();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                rowNum++;
                try {
                    String name = validations.validateString(getCell(row, headerMap, "university name"));
                    if (name == null || name.isBlank()) {
                        log.debug("Row {}: university name empty -> skip", rowNum);
                        result.getRowErrors().add(new RowError(rowNum, "university name required", cellRowSnippet(row)));
                        continue;
                    }

                    College college = new College();
                    college.setName(name);
                    college.setCampusName(validations.validateString(getCell(row, headerMap, "campus")));
                    college.setCampusCode(validations.validateString(getCell(row, headerMap, "campus code")));
                    college.setWebsiteUrl(validations.validateString(getCell(row, headerMap, "website url")));
                    college.setCollegeLogo(validations.validateString(getCell(row, headerMap, "college logo")));
                    college.setCountry(validations.validateString(getCell(row, headerMap, "country")));
                    Integer established = validations.validateInteger(getCell(row, headerMap, "established year"));
                    if (established != null) college.setEstablishedYear(established);
                    college.setRanking(validations.validateString(getCell(row, headerMap, "ranking")));
                    college.setDescription(validations.validateString(getCell(row, headerMap, "description")));
                    college.setCampusGalleryVideoLink(validations.validateString(getCell(row, headerMap, "campus gallery video link")));

                    result.getItems().add(college);
                } catch (Exception rowEx) {
                    log.warn("Row {} - error parsing college row: {}", rowNum, rowEx.getMessage());
                    result.getRowErrors().add(new RowError(rowNum, "Row parse error: " + rowEx.getMessage(), cellRowSnippet(row)));
                }
            }

            log.info("parseColleges finished. parsed={}, errors={}", result.getItems().size(), result.getRowErrors().size());
            if (!result.getRowErrors().isEmpty()) {
                result.getRowErrors().stream().limit(10).forEach(e -> log.debug("parseColleges sample error: {}", e.toString()));
            }
            return result;

        } catch (ExcelException ee) {
            log.error("ExcelException in parseColleges: {}", ee.getMessage());
            throw ee;
        } catch (Exception e) {
            log.error("Unexpected error in parseColleges", e);
            throw new ExcelException(e.getMessage());
        }
    }

    /** Backward-compatible wrapper */
    public static List<College> convertCollegeExcelIntoList(InputStream inputStream) throws Exception {
        ParseResult<College> pr = parseColleges(inputStream);
        return pr.getItems();
    }

    // -------------------------
    // parseCourses (rich version)
    // -------------------------
    public static ParseResult<CourseRequestDto> parseCourses(InputStream inputStream) throws Exception {
        ParseResult<CourseRequestDto> result = new ParseResult<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet("colleges");
            if (sheet == null) throw new ExcelException("Missing 'colleges' sheet");

            Iterator<Row> iterator = sheet.iterator();
            if (!iterator.hasNext()) throw new ExcelException("'colleges' sheet is empty");

            Row headerRow = iterator.next();
            Map<String,Integer> headerMap = buildHeaderMap(headerRow);
            log.info("parseCourses - detected headers: {}", headerMap.keySet());

            int rowNum = headerRow.getRowNum();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                rowNum++;

                try {
                    String name = validations.validateString(getCell(row, headerMap, "course name"));
                    if (name == null || name.isBlank()) {
                        log.debug("Row {}: course name empty -> skip", rowNum);
                        result.getRowErrors().add(new RowError(rowNum, "course name required", cellRowSnippet(row)));
                        continue;
                    }

                    CourseRequestDto dto = new CourseRequestDto();
                    dto.setName(name);
                    dto.setSpecialization(validations.validateString(getCell(row, headerMap, "specialization")));
                    dto.setDepartment(validations.validateString(getCell(row, headerMap, "department")));

                    String gradLevelRaw = validations.validateString(getCell(row, headerMap, "graduation level"));
                    if (gradLevelRaw == null || gradLevelRaw.isBlank()) {
                        // try study level as fallback
                        gradLevelRaw = validations.validateString(getCell(row, headerMap, "study level"));
                    }
                    if (gradLevelRaw == null || gradLevelRaw.isBlank()) {
                        result.getRowErrors().add(new RowError(rowNum, "graduation level is required", cellRowSnippet(row)));
                        continue;
                    }

                    try {
                        dto.setGraduationLevel(gradLevelRaw.trim().toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        String msg = "Invalid graduation level: '" + gradLevelRaw + "'";
                        log.debug("Row {} invalid graduation level: {}", rowNum, gradLevelRaw);
                        result.getRowErrors().add(new RowError(rowNum, msg, cellRowSnippet(row)));
                        continue;
                    }

                    result.getItems().add(dto);
                } catch (IllegalArgumentException iae) {
                    log.warn("Row {}: validation error: {}", rowNum, iae.getMessage());
                    result.getRowErrors().add(new RowError(rowNum, iae.getMessage(), cellRowSnippet(row)));
                } catch (Exception ex) {
                    log.warn("Row {}: unexpected parse error: {}", rowNum, ex.getMessage());
                    result.getRowErrors().add(new RowError(rowNum, "Row parse error: " + ex.getMessage(), cellRowSnippet(row)));
                }
            }

            // dedupe using name|department|graduationLevel
            List<CourseRequestDto> deduped = dedupeCourses(result.getItems());
            result.setItems(deduped);

            log.info("parseCourses finished. parsed={}, errors={}", result.getItems().size(), result.getRowErrors().size());
            if (!result.getRowErrors().isEmpty()) {
                result.getRowErrors().stream().limit(10).forEach(e -> log.debug("parseCourses sample error: {}", e.toString()));
            }
            return result;

        } catch (ExcelException ee) {
            log.error("ExcelException in parseCourses: {}", ee.getMessage());
            throw ee;
        } catch (Exception e) {
            log.error("Unexpected error in parseCourses", e);
            throw new ExcelException(e.getMessage());
        }
    }

    /** Backwards-compatible method returning List<CourseRequestDto> */
    public static List<CourseRequestDto> convertCourseExcelIntoList(InputStream inputStream) throws Exception {
        ParseResult<CourseRequestDto> pr = parseCourses(inputStream);
        return pr.getItems();
    }

    private static List<CourseRequestDto> dedupeCourses(List<CourseRequestDto> list) {
        if (list == null || list.isEmpty()) return list;
        Map<String, CourseRequestDto> map = new LinkedHashMap<>();
        for (CourseRequestDto c : list) {
            String gradName = c.getGraduationLevel() == null ? "" : c.getGraduationLevel();
            String key = (c.getName() == null ? "" : c.getName().trim().toLowerCase()) + "|" +
                    (c.getDepartment() == null ? "" : c.getDepartment().trim().toLowerCase()) + "|" +
                    gradName;
            if (!map.containsKey(key)) map.put(key, c);
        }
        return new ArrayList<>(map.values());
    }

    // -------------------------
    // parseCollegeCourses (rich version)
    // -------------------------
    public static ParseResult<CollegeCourseRequestExcelDto> parseCollegeCourses(InputStream inputStream) throws Exception {
        ParseResult<CollegeCourseRequestExcelDto> result = new ParseResult<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet("colleges");
            if (sheet == null) throw new ExcelException("Missing 'colleges' sheet");

            Iterator<Row> iterator = sheet.iterator();
            if (!iterator.hasNext()) throw new ExcelException("'colleges' sheet is empty");

            Row headerRow = iterator.next();
            Map<String,Integer> headerMap = buildHeaderMap(headerRow);
            log.info("parseCollegeCourses - detected headers: {}", headerMap.keySet());

            // preview
            for (int r = headerRow.getRowNum() + 1; r <= headerRow.getRowNum() + 3 && r <= sheet.getLastRowNum(); r++) {
                Row preview = sheet.getRow(r);
                if (preview != null) log.debug("parseCollegeCourses preview row {} => {}", r + 1, cellRowSnippet(preview));
            }

            int rowNum = headerRow.getRowNum();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                rowNum++;
                try {
                    String campusCode = validations.validateString(getCell(row, headerMap, "campus code"));
                    String courseName = validations.validateString(getCell(row, headerMap, "course name"));
                    String department = validations.validateString(getCell(row, headerMap, "department"));

                    // Try graduation level canonical keys in order: "graduation level", "study level", "graduation"
                    String gradLevel = validations.validateString(getCell(row, headerMap, "graduation level"));
                    if (gradLevel == null || gradLevel.isBlank()) gradLevel = validations.validateString(getCell(row, headerMap, "study level"));
                    if (gradLevel == null || gradLevel.isBlank()) gradLevel = validations.validateString(getCell(row, headerMap, "graduation"));

                    List<String> problems = new ArrayList<>();
                    if (campusCode == null || campusCode.isBlank()) problems.add("campus code required");
                    if (courseName == null || courseName.isBlank()) problems.add("course name required");
                    if (gradLevel == null || gradLevel.isBlank()) problems.add("graduation level required");

                    if (!problems.isEmpty()) {
                        result.getRowErrors().add(new RowError(rowNum, String.join("; ", problems), cellRowSnippet(row)));
                        continue; // skip bad row
                    }

                    CollegeCourseRequestExcelDto dto = new CollegeCourseRequestExcelDto();
                    dto.setCampusCode(campusCode);
                    dto.setCourseName(courseName);
                    dto.setDepartment(department);
                    dto.setGraduationLevel(gradLevel.toUpperCase());

                    // course url: try "course url" canonical, fallback to "website url"
                    String courseUrl = validations.validateString(getCell(row, headerMap, "course url"));
                    if (courseUrl == null) courseUrl = validations.validateString(getCell(row, headerMap, "website url"));
                    dto.setCourseUrl(courseUrl);

                    // Duration conversions
                    String durationRaw = validations.validateString(getCell(row, headerMap, "duration"));
                    if (durationRaw != null) {
                        try {
                            Integer months = FormatConverter.cnvrtDurationToInteger(durationRaw);
                            dto.setDuration(months == null ? null : months.toString());
                        } catch (Exception ex) {
                            result.getRowErrors().add(new RowError(rowNum, "Invalid duration: " + durationRaw, cellRowSnippet(row)));
                            continue;
                        }
                    }

                    dto.setIntakeMonths(validations.validateString(getCell(row, headerMap, "intake month")));
                    dto.setIntakeYear(validations.validateInteger(getCell(row, headerMap, "intake year")));
                    dto.setEligibilityCriteria(validations.validateString(getCell(row, headerMap, "eligibility criteria")));
                    dto.setApplicationFee(validations.validateString(getCell(row, headerMap, "application fee")));

                    // Tuition: try yearly tuition fees then tuition fee
                    Double tuition = validations.validateDouble(getCell(row, headerMap, "yearly tuition fees"));
                    if (tuition == null) tuition = validations.validateDouble(getCell(row, headerMap, "tuition fee"));
                    if (tuition != null) dto.setTuitionFee(String.valueOf(tuition));
                    else dto.setTuitionFee(validations.validateString(getCell(row, headerMap, "yearly tuition fees")));

                    // Scores mapping (use canonical keys)
                    dto.setIeltsMinScore(validations.validateDouble(getCell(row, headerMap, "ielts score")));
                    dto.setIeltsMinBandScore(validations.validateDouble(getCell(row, headerMap, "ielts no band less than")));
                    dto.setPteMinScore(validations.validateDouble(getCell(row, headerMap, "pte score")));
                    dto.setPteMinBandScore(validations.validateDouble(getCell(row, headerMap, "pte no band less than")));
                    dto.setToeflMinScore(validations.validateDouble(getCell(row, headerMap, "tofel score")));
                    dto.setToeflMinBandScore(validations.validateDouble(getCell(row, headerMap, "tofel band")));
                    dto.setGreMinScore(validations.validateDouble(getCell(row, headerMap, "gre score")));
                    dto.setGmatMinScore(validations.validateDouble(getCell(row, headerMap, "gmat score")));
                    dto.setSatMinScore(validations.validateDouble(getCell(row, headerMap, "sat score")));
                    // dto.setCatMinScore(validations.validateDouble(getCell(row, headerMap, "cat min score")));

                    // education level numeric scores
                    dto.setMin10thScore(validations.validateDouble(getCell(row, headerMap, "10th")));
                    dto.setMinInterScore(validations.validateDouble(getCell(row, headerMap, "inter")));
                    dto.setMinGraduationScore(validations.validateDouble(getCell(row, headerMap, "graduation")));

                    dto.setScholarshipEligible(validations.validateString(getCell(row, headerMap, "scholarship")));
                    dto.setScholarshipDetails(validations.validateString(getCell(row, headerMap, "scholarship details")));
                    dto.setBacklogAcceptanceRange(validations.validateString(getCell(row, headerMap, "backlog range")));
                    dto.setRemarks(validations.validateString(getCell(row, headerMap, "remarks")));

                    result.getItems().add(dto);
                } catch (IllegalArgumentException iae) {
                    log.warn("Row {} validation error: {}", rowNum, iae.getMessage());
                    result.getRowErrors().add(new RowError(rowNum, iae.getMessage(), cellRowSnippet(row)));
                } catch (Exception ex) {
                    log.warn("Row {} parse error: {}", rowNum, ex.getMessage());
                    result.getRowErrors().add(new RowError(rowNum, "Row parse error: " + ex.getMessage(), cellRowSnippet(row)));
                }
            }

            log.info("parseCollegeCourses finished. parsed={}, errors={}", result.getItems().size(), result.getRowErrors().size());
            if (!result.getRowErrors().isEmpty()) result.getRowErrors().stream().limit(10).forEach(e -> log.debug("parseCollegeCourses sample error: {}", e.toString()));
            return result;

        } catch (ExcelException ee) {
            log.error("ExcelException in parseCollegeCourses: {}", ee.getMessage());
            throw ee;
        } catch (Exception e) {
            log.error("Unexpected error in parseCollegeCourses", e);
            throw new ExcelException(e.getMessage());
        }
    }

    /** Backwards-compatible method returning List<CollegeCourseRequestExcelDto> */
    public static List<CollegeCourseRequestExcelDto> convertCollegeCourseExcelIntoList(InputStream inputStream) throws Exception {
        ParseResult<CollegeCourseRequestExcelDto> pr = parseCollegeCourses(inputStream);
        return pr.getItems();
    }
}

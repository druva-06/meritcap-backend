package com.consultancy.education.utils;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced BasicValidations utility.
 * - Keeps backward-compatible methods used by existing code.
 * - Adds stricter "required" validators that throw IllegalArgumentException (useful for Excel row validation).
 * - Adds flexible parsers (percentage, durations, flexible numeric parsing).
 *
 * Usage:
 *  - For optional fields continue to use validateString/validateInteger/validateDouble which return null if not present/invalid.
 *  - For required fields use validateStringRequired(...), validateIntegerRequired(...), etc. which throw IllegalArgumentException with a clear message.
 */
public class BasicValidations {

    private static final Logger log = LoggerFactory.getLogger(BasicValidations.class);

    private static final Pattern PERCENT_PATTERN = Pattern.compile("^\\s*([+-]?[0-9]+(?:[.,][0-9]+)?)\\s*%?\\s*$");
    private static final Pattern DURATION_YEARS_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(years|year|yrs|yr|y)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION_MONTHS_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(months|month|mos|mo|m)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMERIC_CLEANUP = Pattern.compile("[^0-9+\\-.,]");

    /**
     * Original behavior retained: return parsed object for cell (String or Double) or null.
     */
    public Object cellValidation(Cell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        try {
            if (type == CellType.STRING) {
                String value = cell.getStringCellValue();
                if (value == null) return null;
                value = value.trim();
                if (value.isEmpty() || value.equalsIgnoreCase("NA")) {
                    return null;
                }
                return value;
            } else if (type == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (type == CellType.BLANK) {
                return null;
            } else if (type == CellType.FORMULA) {
                return cellToString(cell);
            } else if (type == CellType.BOOLEAN) {
                return cell.getBooleanCellValue();
            }
        } catch (Exception e) {
            log.debug("cellValidation: failed to parse cell - returning null", e);
        }
        return null;
    }

    /**
     * Original behavior retained: validate string cell, return trimmed string or null.
     */
    public String validateString(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty() || value.equalsIgnoreCase("NA")) {
                    return null;
                }
                return value;
            } else if (cell.getCellType() == CellType.FORMULA) {
                String v = cellToString(cell);
                if (v == null || v.trim().isEmpty() || v.equalsIgnoreCase("NA")) return null;
                return v.trim();
            }
        } catch (Exception e) {
            log.debug("validateString: unable to parse cell as string", e);
        }
        return null;
    }

    /**
     * Original behavior retained: validate integer from numeric cell only.
     */
    public Integer validateInteger(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty() || s.equalsIgnoreCase("NA")) return null;
                try {
                    // tolerant parse
                    Double d = Double.valueOf(s.replaceAll(",", ""));
                    return d.intValue();
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else if (cell.getCellType() == CellType.FORMULA) {
                String s = cellToString(cell);
                if (s != null) {
                    try {
                        Double d = Double.valueOf(s.replaceAll(",", ""));
                        return d.intValue();
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("validateInteger: error parsing cell", e);
        }
        return null;
    }

    /**
     * Original behavior retained: validate double from numeric cell only (but now accepts string that parses).
     */
    public Double validateDouble(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty() || s.equalsIgnoreCase("NA")) return null;
                return parseDoubleFlexibleString(s);
            } else if (cell.getCellType() == CellType.FORMULA) {
                String s = cellToString(cell);
                if (s == null) return null;
                return parseDoubleFlexibleString(s);
            }
        } catch (Exception e) {
            log.debug("validateDouble: error parsing cell", e);
        }
        return null;
    }

    // -----------------------------
    // New stricter/utility methods
    // -----------------------------

    /**
     * Return true if cell is null/blank/"NA".
     */
    public boolean isBlank(Cell cell) {
        if (cell == null) return true;
        if (cell.getCellType() == CellType.BLANK) return true;
        if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue();
            return v == null || v.trim().isEmpty() || v.trim().equalsIgnoreCase("NA");
        }
        return false;
    }

    /**
     * Required string - throws IllegalArgumentException with field context if missing/invalid.
     */
    public String validateStringRequired(Cell cell, String fieldName) {
        String v = validateString(cell);
        if (v == null) {
            String msg = fieldName + " is required";
            log.debug("validateStringRequired failed: {}", msg);
            throw new IllegalArgumentException(msg);
        }
        return v;
    }

    /**
     * Required integer - throws IllegalArgumentException with field context if missing/invalid.
     */
    public Integer validateIntegerRequired(Cell cell, String fieldName) {
        Integer v = validateInteger(cell);
        if (v == null) {
            String msg = fieldName + " is required and must be integer";
            log.debug("validateIntegerRequired failed: {}", msg);
            throw new IllegalArgumentException(msg);
        }
        return v;
    }

    /**
     * Required double - throws IllegalArgumentException with field context if missing/invalid.
     */
    public Double validateDoubleRequired(Cell cell, String fieldName) {
        Double v = validateDouble(cell);
        if (v == null) {
            String msg = fieldName + " is required and must be numeric";
            log.debug("validateDoubleRequired failed: {}", msg);
            throw new IllegalArgumentException(msg);
        }
        return v;
    }

    /**
     * Parse double from string flexibly (commas allowed, percentage stripped etc).
     * Returns null if unable to parse.
     */
    public Double parseDoubleFlexible(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        String s = cellToString(cell);
        if (s == null) return null;
        try {
            return parseDoubleFlexibleString(s);
        } catch (Exception e) {
            log.debug("parseDoubleFlexible: cannot parse '{}' -> null", s);
            return null;
        }
    }

    private Double parseDoubleFlexibleString(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("NA")) return null;
        // strip currency symbols / words
        s = s.replaceAll("(?i)[^0-9,\\-+.%]", "");
        // handle percentage like "85%"
        Matcher m = PERCENT_PATTERN.matcher(s);
        if (m.matches()) {
            String num = m.group(1).replace(",", "");
            try {
                double val = NumberFormat.getInstance(Locale.US).parse(num).doubleValue();
                // if percentage format present, return as-is (e.g., 85% -> 85.0)
                return val;
            } catch (ParseException e) {
                return null;
            }
        }
        // try parse plain number
        try {
            String cleaned = s.replaceAll(",", "");
            return Double.valueOf(cleaned);
        } catch (NumberFormatException ex) {
            // last attempt using NumberFormat
            try {
                Number n = NumberFormat.getInstance(Locale.US).parse(s);
                return n.doubleValue();
            } catch (ParseException pe) {
                log.debug("parseDoubleFlexibleString parse failed for '{}'", s, pe);
                return null;
            }
        }
    }

    /**
     * Parse integer flexibly from numeric or string cell.
     */
    public Integer parseIntegerFlexible(Cell cell) {
        Double d = parseDoubleFlexible(cell);
        return d == null ? null : d.intValue();
    }

    /**
     * Parse percentage values. Accepts:
     *   - numeric cell (e.g., 85 -> 85.0)
     *   - string with percent sign "85%" or "85.5%"
     *   - string without percent "85" or "85.5"
     * Returns Double (raw percentage value), or null if unparsable.
     */
    public Double parsePercentage(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        String s = cellToString(cell);
        if (s == null) return null;
        Matcher m = PERCENT_PATTERN.matcher(s);
        if (m.matches()) {
            String num = m.group(1).replace(",", "");
            try {
                return Double.valueOf(num);
            } catch (NumberFormatException e) {
                try {
                    return NumberFormat.getInstance(Locale.US).parse(num).doubleValue();
                } catch (ParseException pe) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Parse a duration expression into months.
     * Examples:
     *   "2 years" -> 24
     *   "2.5 years" -> 30
     *   "24 months" -> 24
     *   "36" -> 36 (interpreted as months)
     *
     * Returns null if not parseable.
     */
    public Integer parseDurationMonths(Cell cell) {
        if (cell == null) return null;
        String s = cellToString(cell);
        if (s == null) return null;
        s = s.trim().toLowerCase();

        // try months pattern first
        Matcher mMonths = DURATION_MONTHS_PATTERN.matcher(s);
        if (mMonths.find()) {
            try {
                double val = Double.parseDouble(mMonths.group(1));
                return (int) Math.round(val);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // try years
        Matcher mYears = DURATION_YEARS_PATTERN.matcher(s);
        if (mYears.find()) {
            try {
                double years = Double.parseDouble(mYears.group(1));
                return (int) Math.round(years * 12);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // if pure number provided, treat as months (or user can call parseIntegerFlexible)
        try {
            String cleaned = NUMERIC_CLEANUP.matcher(s).replaceAll("");
            if (cleaned.isEmpty()) return null;
            double val = Double.parseDouble(cleaned);
            return (int) Math.round(val);
        } catch (Exception e) {
            log.debug("parseDurationMonths: cannot parse '{}'", s);
            return null;
        }
    }

    /**
     * Parse a comma separated intake months cell into list of strings (tokens).
     * e.g., "Jan, Feb, Mar" -> ["Jan","Feb","Mar"]
     * Keep tokens trimmed and non-empty.
     */
    public List<String> parseIntakeMonthsAsStrings(Cell cell) {
        String s = cellToString(cell);
        if (s == null) return Collections.emptyList();
        String[] parts = s.split("[,;/]");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    /**
     * Normalize and extract a safe string from any cell type.
     * - Returns trimmed string, or null for blanks.
     */
    public String toSafeString(Cell cell) {
        String v = cellToString(cell);
        if (v == null) return null;
        v = v.trim();
        if (v.isEmpty() || v.equalsIgnoreCase("NA")) return null;
        return v;
    }

    /**
     * Convert cell to string representation in a safe manner.
     * Handles FORMULA, NUMERIC, BOOLEAN, STRING.
     */
    public String cellToString(Cell cell) {
        if (cell == null) return null;
        try {
            CellType t = cell.getCellType();
            switch (t) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // return ISO-like date/time string for visibility
                        return cell.getLocalDateTimeCellValue().toString();
                    }
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) return String.valueOf((long) d);
                    return String.valueOf(d);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        try {
                            double dv = cell.getNumericCellValue();
                            if (dv == Math.floor(dv)) return String.valueOf((long) dv);
                            return String.valueOf(dv);
                        } catch (Exception ex) {
                            return cell.getCellFormula();
                        }
                    }
                case BLANK:
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.debug("cellToString: failed to convert cell to string", e);
            return null;
        }
    }
}

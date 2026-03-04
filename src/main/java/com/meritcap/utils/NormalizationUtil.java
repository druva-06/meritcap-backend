package com.meritcap.utils;

import com.meritcap.enums.Month;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public final class NormalizationUtil {
    private NormalizationUtil() {
    }

    private static final Map<String, String> GRAD_MAP = new HashMap<>();
    static {
        // common synonyms -> canonical short string
        GRAD_MAP.put("bachelor", "BACHELOR");
        GRAD_MAP.put("bachelors", "BACHELOR");
        GRAD_MAP.put("ug", "BACHELOR");
        GRAD_MAP.put("undergraduate", "BACHELOR");
        GRAD_MAP.put("master", "MASTER");
        GRAD_MAP.put("masters", "MASTER");
        GRAD_MAP.put("msc", "MASTER");
        GRAD_MAP.put("ms", "MASTER");
        GRAD_MAP.put("postgraduate", "MASTER");
        GRAD_MAP.put("phd", "PHD");
        GRAD_MAP.put("doctoral", "PHD");
        // add more if needed
    }

    public static String normalizeGraduationLevel(String raw) {
        if (raw == null)
            return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty())
            return null;
        for (Map.Entry<String, String> e : GRAD_MAP.entrySet()) {
            if (s.contains(e.getKey()))
                return e.getValue();
        }
        // fallback: uppercased trimmed token (single word)
        return s.toUpperCase().replaceAll("\\s+", "_");
    }

    public static String genSlug(String name, String campus) {
        String base = (name == null ? "" : name.trim()) + (campus == null ? "" : " " + campus.trim());
        base = base.trim().toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
        if (base.isBlank())
            base = "college-" + UUID.randomUUID().toString().substring(0, 8);
        if (base.length() > 160)
            base = base.substring(0, 160);
        return base;
    }

    public static String normalizeCampusCode(String raw) {
        if (raw == null)
            return null;
        String s = raw.trim();
        if (s.isEmpty())
            return null;
        return s.toUpperCase();
    }

    public static Integer parseDurationMonths(String raw) {
        if (raw == null)
            return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty())
            return null;
        // examples: "36 Months", "3 Years", "3 yrs", "36"
        try {
            if (s.matches(".*\\d+.*")) {
                // extract first number
                String digits = s.replaceAll("[^0-9]", " ").trim().split("\\s+")[0];
                int n = Integer.parseInt(digits);
                if (s.contains("year") || s.contains("yr"))
                    return n * 12;
                return n;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static List<Month> parseIntakeMonthsToEnum(String raw) {
        if (raw == null)
            return Collections.emptyList();
        String[] parts = raw.split("[,;|/]");
        List<Month> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty())
                continue;
            // numeric month
            try {
                int mn = Integer.parseInt(t);
                if (mn >= 1 && mn <= 12) {
                    out.add(Month.values()[mn - 1]); // enum uses JAN..DEC
                    continue;
                }
            } catch (NumberFormatException ignored) {
            }
            // 3-letter or full
            String up = t.trim().toUpperCase();
            // try 3-letter mapping
            try {
                if (up.length() >= 3) {
                    String three = up.substring(0, 3);
                    Month m = Month.valueOf(three);
                    out.add(m);
                    continue;
                }
            } catch (Exception ignored) {
            }
            // fallback: match by startsWith with enum names
            for (Month m : Month.values()) {
                if (m.name().startsWith(up) || m.name().contains(up)) {
                    out.add(m);
                    break;
                }
            }
        }
        // dedupe preserving order
        return out.stream().distinct().collect(Collectors.toList());
    }

    public static Integer defaultIntakeYearIfMissing(Integer value) {
        if (value == null)
            return 0; // as per your instruction
        return value;
    }

    public static int currentYear() {
        return Year.now().getValue();
    }

    public static String nullifyIfEmpty(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("NA"))
            return null;
        return t;
    }

    /**
     * Strip invisible Unicode characters (zero-width space, soft hyphen, etc.)
     * that can cause duplicate-detection mismatches.
     */
    public static String stripInvisibleChars(String s) {
        if (s == null)
            return null;
        // Remove zero-width space (U+200B), zero-width no-break space (U+FEFF),
        // zero-width joiner (U+200D), zero-width non-joiner (U+200C),
        // soft hyphen (U+00AD), and other common zero-width chars
        return s.replaceAll("[\\u200B\\u200C\\u200D\\uFEFF\\u00AD\\u2060]", "")
                .replace('\u00A0', ' ') // non-breaking space → regular space
                .replaceAll("\\s+", " ") // collapse multiple whitespace to single space
                .trim();
    }

    public static Double parseDoubleOrNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("NA"))
            return null;
        try {
            return Double.parseDouble(t.replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate a deterministic campus code from university name + campus.
     * Used when the Excel sheet doesn't have a campus code column.
     * E.g. "University of Oxford", "Main Campus" -> "UNIOFOXF_MAIN"
     */
    public static String generateCampusCode(String universityName, String campus) {
        if (universityName == null || universityName.trim().isEmpty())
            return null;
        String base = universityName.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (base.length() > 12)
            base = base.substring(0, 12);
        if (campus != null && !campus.trim().isEmpty()) {
            String c = campus.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (c.length() > 8)
                c = c.substring(0, 8);
            base = base + "_" + c;
        }
        return base;
    }
}

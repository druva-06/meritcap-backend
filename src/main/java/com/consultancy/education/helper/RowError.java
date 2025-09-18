package com.consultancy.education.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Row-level error holder.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RowError {
    private int rowNumber;         // 1-based row number in Excel (including header)
    private String message;        // human-friendly message
    private String rawRowSnippet;  // optional: small snippet / joined cell values for debugging
}

package com.consultancy.education.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic parsing result: parsed objects + row-level errors (if any).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult<T> {
    private List<T> items = new ArrayList<>();
    private List<RowError> rowErrors = new ArrayList<>();

    public boolean hasErrors() {
        return rowErrors != null && !rowErrors.isEmpty();
    }
}

package com.consultancy.education.DTOs.responseDTOs.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCourseResponseDto<T> {
    List<T> data;
    Pagination pagination;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Pagination {
        int currentPage;
        int pageSize;
        int totalPages;
        long totalItems;
    }
}

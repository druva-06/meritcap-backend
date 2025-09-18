package com.consultancy.education.DTOs.responseDTOs.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned when a bulk upload job is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponseDto {
    private Long jobId;
    private String fileName;
    private String status;
}

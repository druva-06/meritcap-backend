package com.meritcap.DTOs.responseDTOs.bulk;

import com.meritcap.model.BulkUploadJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for exposing the status of a bulk upload job.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadStatusDto {
    private Long jobId;
    private String fileName;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer percentComplete;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

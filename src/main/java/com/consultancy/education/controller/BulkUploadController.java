package com.consultancy.education.controller;

import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadResponseDto;
import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadStatusDto;
import com.consultancy.education.service.BulkUploadService;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.Collections;

@RestController
@RequestMapping("/bulk/upload")
@Tag(name = "Bulk Upload", description = "Bulk upload for Colleges, Courses and CollegeCourse mappings")
public class BulkUploadController {

    private static final Logger log = LoggerFactory.getLogger(BulkUploadController.class);

    private final BulkUploadService bulkUploadService;

    // sensible max file size (bytes) - adjust or externalize to config
    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L; // 50 MB

    public BulkUploadController(BulkUploadService bulkUploadService) {
        this.bulkUploadService = bulkUploadService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Start combined bulk upload", description = "Starts background processing of the provided .xlsx file containing colleges, courses and college-course mappings. Returns jobId.")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> startUpload(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal Jwt jwt) {
        String currentUser = (jwt == null) ? "system" : jwt.getClaimAsString("sub");
        String originalFilename = (file != null) ? StringUtils.cleanPath(file.getOriginalFilename()) : "N/A";
        long fileSize = (file != null) ? file.getSize() : 0;

        log.info("BulkUploadController.startUpload invoked user={} filename={} size={} bytes", currentUser, originalFilename, fileSize);

        try {
            if (file == null || file.isEmpty()) {
                log.warn("Validation failed: Empty file received user={} filename={}", currentUser, originalFilename);
                return ResponseEntity.badRequest().body(new ApiFailureResponse<>(Collections.emptyList(), "No file provided", 400));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("Validation failed: File too large user={} filename={} size={} bytes", currentUser, originalFilename, file.getSize());
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(new ApiFailureResponse<>(Collections.emptyList(), "File too large (max " + (MAX_FILE_SIZE / (1024*1024)) + " MB)", 413));
            }

            if (!com.consultancy.education.helper.ExcelHelper.checkExcelFormat(file)) {
                log.warn("Validation failed: Incorrect Excel format user={} filename={}", currentUser, originalFilename);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(Collections.emptyList(), "Incorrect excel format; expected .xlsx/.xls", 400));
            }

            BulkUploadResponseDto resp = bulkUploadService.startBulkUpload(file, currentUser);
            log.info("Bulk upload job created successfully user={} filename={} jobId={}", currentUser, originalFilename, resp.getJobId());

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(resp, "Bulk upload started", 201));

        } catch (IllegalArgumentException ex) {
            log.warn("Validation exception user={} filename={} error={}", currentUser, originalFilename, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(Collections.emptyList(), ex.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error starting bulk upload user={} filename={} size={} bytes", currentUser, originalFilename, fileSize, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(Collections.emptyList(), "Failed to start bulk upload", 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{jobId}/status")
    @Operation(summary = "Get bulk job status", description = "Returns current progress for the given jobId")
    public ResponseEntity<?> getStatus(@PathVariable("jobId") Long jobId,
                                       @AuthenticationPrincipal Jwt jwt) {
        String currentUser = (jwt == null) ? "system" : jwt.getClaimAsString("sub");
        log.info("BulkUploadController.getStatus invoked user={} jobId={}", currentUser, jobId);

        try {
            BulkUploadStatusDto status = bulkUploadService.getStatus(jobId);
            log.debug("Fetched job status successfully user={} jobId={} status={}", currentUser, jobId, status.getStatus());
            return ResponseEntity.ok(new ApiSuccessResponse<>(status, "Status fetched", 200));
        } catch (java.util.NoSuchElementException e) {
            log.warn("Job not found user={} jobId={}", currentUser, jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(Collections.emptyList(), "Job not found", 404));
        } catch (Exception e) {
            log.error("Error fetching job status user={} jobId={}", currentUser, jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(Collections.emptyList(), "Failed to fetch status", 500));
        }
    }
}

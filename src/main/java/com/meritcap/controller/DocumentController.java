package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.document.DocumentUploadRequestDto;
import com.meritcap.DTOs.responseDTOs.document.DocumentResponseDto;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.exception.ValidationException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.DocumentService;
import com.meritcap.utils.ToMap;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public DocumentController(DocumentService documentService, AuthenticatedUserResolver authenticatedUserResolver) {
        this.documentService = documentService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestPart("metadata") @Valid DocumentUploadRequestDto requestDto,
            BindingResult bindingResult,
            @RequestPart("file") MultipartFile file,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", ToMap.bindingResultToMap(bindingResult));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            String uploadedBy = principal.getName();
            DocumentResponseDto responseDto = documentService.uploadDocument(requestDto, file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiSuccessResponse<>(responseDto, "Document uploaded successfully", 201));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Document upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> listDocuments(
            @RequestParam String referenceType,
            @RequestParam Long referenceId
    ) {
        try {
            if (!authenticatedUserResolver.isCurrentUserAdmin()) {
                if (!"STUDENT".equalsIgnoreCase(referenceType)) {
                    throw new CustomException("You do not have permission to access this resource");
                }
                authenticatedUserResolver.assertCurrentStudentOwns(referenceId);
            }
            List<DocumentResponseDto> docs = documentService.getDocuments(referenceType, referenceId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(docs, "Documents fetched successfully", 200));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Failed to list documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @PutMapping("/admin/{documentId}/status")
    public ResponseEntity<?> updateDocumentStatus(
            @PathVariable Long documentId,
            @RequestParam String status,
            @RequestParam(required = false) String remarks
    ) {
        log.info("Admin: Update document status - documentId={}, status={}", documentId, status);
        try {
            DocumentResponseDto responseDto = documentService.updateDocumentStatus(documentId, status, remarks);
            return ResponseEntity.ok(new ApiSuccessResponse<>(responseDto, "Document status updated successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Failed to update document status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'COUNSELOR')")
    @GetMapping("/{documentId}/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@PathVariable Long documentId, Principal principal) {
        log.info("Presigned URL requested for documentId={}, by={}", documentId, principal != null ? principal.getName() : "unknown");
        try {
            String presignedUrl = documentService.generatePresignedUrl(documentId,
                    principal != null ? principal.getName() : null);
            return ResponseEntity.ok(new ApiSuccessResponse<>(presignedUrl, "Presigned URL generated", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for documentId={}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiFailureResponse<>(new ArrayList<>(), "Failed to generate URL", 500));
        }
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @DeleteMapping("/delete/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId, Principal principal) {
        try {
            documentService.deleteDocument(documentId, principal.getName());
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Document deleted successfully", 200));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Failed to delete document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.document.DocumentUploadRequestDto;
import com.meritcap.DTOs.responseDTOs.document.DocumentResponseDto;
import com.meritcap.exception.CustomException;
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

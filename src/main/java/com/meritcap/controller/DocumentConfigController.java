package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.documentconfig.*;
import com.meritcap.DTOs.responseDTOs.documentconfig.*;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.DocumentConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document-config")
@RequiredArgsConstructor
@Slf4j
public class DocumentConfigController {

    private final DocumentConfigService documentConfigService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    // =====================================================================
    // Document Types
    // =====================================================================

    @GetMapping("/document-types")
    public ResponseEntity<?> getAllDocumentTypes() {
        try {
            List<DocumentTypeResponseDto> result = documentConfigService.getAllDocumentTypes();
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Document types fetched", 200));
        } catch (Exception e) {
            log.error("Error fetching document types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @GetMapping("/document-types/{id}")
    public ResponseEntity<?> getDocumentTypeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    documentConfigService.getDocumentTypeById(id), "Document type fetched", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching document type id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PostMapping("/document-types")
    public ResponseEntity<?> createDocumentType(@RequestBody @Valid DocumentTypeRequestDto requestDto) {
        try {
            DocumentTypeResponseDto result = documentConfigService.createDocumentType(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(result, "Document type created", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error creating document type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PutMapping("/document-types/{id}")
    public ResponseEntity<?> updateDocumentType(@PathVariable Long id,
                                                @RequestBody @Valid DocumentTypeRequestDto requestDto) {
        try {
            DocumentTypeResponseDto result = documentConfigService.updateDocumentType(id, requestDto);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Document type updated", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error updating document type id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @DeleteMapping("/document-types/{id}")
    public ResponseEntity<?> deleteDocumentType(@PathVariable Long id) {
        try {
            documentConfigService.deleteDocumentType(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Document type deleted", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting document type id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    // =====================================================================
    // Profile Document Requirements
    // =====================================================================

    @GetMapping("/profile-requirements")
    public ResponseEntity<?> getProfileRequirements() {
        try {
            List<ProfileDocumentRequirementResponseDto> result = documentConfigService.getProfileRequirements();
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Profile requirements fetched", 200));
        } catch (Exception e) {
            log.error("Error fetching profile requirements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PostMapping("/profile-requirements")
    public ResponseEntity<?> createProfileRequirement(
            @RequestBody @Valid ProfileDocumentRequirementRequestDto requestDto) {
        try {
            ProfileDocumentRequirementResponseDto result =
                    documentConfigService.createProfileRequirement(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(result, "Profile requirement created", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error creating profile requirement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PutMapping("/profile-requirements/{id}")
    public ResponseEntity<?> updateProfileRequirement(@PathVariable Long id,
                                                      @RequestBody @Valid ProfileDocumentRequirementRequestDto requestDto) {
        try {
            ProfileDocumentRequirementResponseDto result =
                    documentConfigService.updateProfileRequirement(id, requestDto);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Profile requirement updated", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error updating profile requirement id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @DeleteMapping("/profile-requirements/{id}")
    public ResponseEntity<?> deleteProfileRequirement(@PathVariable Long id) {
        try {
            documentConfigService.deleteProfileRequirement(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Profile requirement deleted", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting profile requirement id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    // =====================================================================
    // Country Document Requirements
    // =====================================================================

    @GetMapping("/country-requirements/{countryId}")
    public ResponseEntity<?> getCountryRequirements(@PathVariable Long countryId) {
        try {
            List<CountryDocumentRequirementResponseDto> result =
                    documentConfigService.getCountryRequirements(countryId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Country requirements fetched", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching country requirements countryId={}", countryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PostMapping("/country-requirements")
    public ResponseEntity<?> createCountryRequirement(
            @RequestBody @Valid CountryDocumentRequirementRequestDto requestDto) {
        try {
            CountryDocumentRequirementResponseDto result =
                    documentConfigService.createCountryRequirement(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(result, "Country requirement created", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error creating country requirement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PutMapping("/country-requirements/{id}")
    public ResponseEntity<?> updateCountryRequirement(@PathVariable Long id,
                                                      @RequestBody @Valid CountryDocumentRequirementRequestDto requestDto) {
        try {
            CountryDocumentRequirementResponseDto result =
                    documentConfigService.updateCountryRequirement(id, requestDto);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Country requirement updated", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error updating country requirement id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @DeleteMapping("/country-requirements/{id}")
    public ResponseEntity<?> deleteCountryRequirement(@PathVariable Long id) {
        try {
            documentConfigService.deleteCountryRequirement(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Country requirement deleted", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting country requirement id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PostMapping("/country-requirements/bulk")
    public ResponseEntity<?> bulkSaveCountryRequirements(
            @RequestBody @Valid CountryDocumentRequirementBulkRequestDto requestDto) {
        try {
            List<CountryDocumentRequirementResponseDto> result =
                    documentConfigService.bulkSaveCountryRequirements(requestDto);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Country requirements saved", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error bulk-saving country requirements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    // =====================================================================
    // Compliance check (student-facing)
    // =====================================================================

    @GetMapping("/compliance/country/{countryId}/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> checkCompliance(@PathVariable Long countryId,
                                             @PathVariable Long studentId) {
        try {
            DocumentComplianceResponseDto result =
                    documentConfigService.checkCountryDocumentCompliance(studentId, countryId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Compliance checked", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error checking compliance countryId={}, studentId={}", countryId, studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }
}

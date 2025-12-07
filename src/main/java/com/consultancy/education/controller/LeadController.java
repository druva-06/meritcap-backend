package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadRequestDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.LeadService;
import com.consultancy.education.utils.ToMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("leads")
@Tag(name = "Lead Management", description = "APIs for managing leads in the system")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    @Operation(summary = "Create a new lead", description = "Creates a new lead with encrypted sensitive information")
    public ResponseEntity<?> createLead(
            @RequestBody @Valid LeadRequestDto leadRequestDto,
            BindingResult bindingResult) {

        log.info("Create lead request received for email: {}", leadRequestDto.getEmail());

        // Validate request
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in lead creation request");
            Map<String, String> errors = ToMap.bindingResultToMap(bindingResult);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(errors, "Validation failed", 400));
        }

        try {
            // Get authenticated user email from SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = getEmailFromAuthentication(authentication);

            // Create lead
            LeadResponseDto response = leadService.createLead(leadRequestDto, userEmail);

            log.info("Lead created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Lead created successfully", 201));

        } catch (CustomException e) {
            log.error("Custom exception during lead creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));

        } catch (NotFoundException e) {
            log.error("Not found exception during lead creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));

        } catch (Exception e) {
            log.error("Unexpected error during lead creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Extract user email from authentication context
     */
    private String getEmailFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.error("Authentication is null or has no principal");
            throw new CustomException("User not authenticated");
        }

        String email = authentication.getName();
        log.debug("Extracted email from authentication: {}", email);
        return email;
    }
}

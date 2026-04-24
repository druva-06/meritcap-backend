package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.lead.DirectAssignRequestDto;
import com.meritcap.DTOs.requestDTOs.lead.LeadFilterDto;
import com.meritcap.DTOs.requestDTOs.lead.LeadRequestDto;
import com.meritcap.DTOs.requestDTOs.lead.ReassignLeadRequestDto;
import com.meritcap.DTOs.requestDTOs.lead.RoundRobinAssignRequestDto;
import com.meritcap.DTOs.requestDTOs.lead.UpdateLeadRequestDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadPageResponseDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadResponseDto;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.LeadService;
import com.meritcap.service.RoundRobinService;
import com.meritcap.utils.ToMap;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("leads")
@Tag(name = "Lead Management", description = "APIs for managing leads in the system")
public class LeadController {

    private final LeadService leadService;
    private final RoundRobinService roundRobinService;

    public LeadController(LeadService leadService, RoundRobinService roundRobinService) {
        this.leadService = leadService;
        this.roundRobinService = roundRobinService;
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

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping
    @Operation(summary = "Get leads with filters and pagination", description = "Retrieves leads based on filter criteria with pagination support")
    public ResponseEntity<?> getLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String campaign,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer scoreFrom,
            @RequestParam(required = false) Integer scoreTo,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Get leads request - search: {}, campaign: {}, page: {}, size: {}",
                search, campaign, page, size);

        try {
            // Build filter DTO
            LeadFilterDto filterDto = LeadFilterDto.builder()
                    .search(search)
                    .campaign(campaign)
                    .dateFrom(dateFrom != null ? java.time.LocalDate.parse(dateFrom) : null)
                    .dateTo(dateTo != null ? java.time.LocalDate.parse(dateTo) : null)
                    .scoreFrom(scoreFrom)
                    .scoreTo(scoreTo)
                    .status(status != null ? status.stream()
                            .map(s -> com.meritcap.enums.LeadStatus.valueOf(s))
                            .collect(java.util.stream.Collectors.toList()) : null)
                    .tags(tags)
                    .assignedTo(assignedTo)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            // Get leads
            LeadPageResponseDto response = leadService.getLeads(filterDto);

            log.info("Successfully fetched {} leads", response.getTotalElements());
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Leads fetched successfully", 200));

        } catch (Exception e) {
            log.error("Error fetching leads: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error fetching leads: " + e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/{id}")
    @Operation(summary = "Get lead by ID", description = "Retrieves complete information of a lead by its ID")
    public ResponseEntity<?> getLeadById(@PathVariable Long id) {
        log.info("Get lead by ID request - ID: {}", id);

        try {
            LeadResponseDto response = leadService.getLeadById(id);
            log.info("Successfully fetched lead with ID: {}", id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Lead fetched successfully", 200));

        } catch (NotFoundException e) {
            log.error("Lead not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));

        } catch (Exception e) {
            log.error("Error fetching lead by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error fetching lead: " + e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @PutMapping("/{id}")
    @Operation(summary = "Update lead by ID", description = "Updates lead information. Email and phone number cannot be changed.")
    public ResponseEntity<?> updateLead(
            @PathVariable Long id,
            @RequestBody @Valid UpdateLeadRequestDto updateLeadRequestDto,
            BindingResult bindingResult) {

        log.info("Update lead request received for ID: {}", id);

        // Validate request
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in lead update request");
            Map<String, String> errors = ToMap.bindingResultToMap(bindingResult);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(errors, "Validation failed", 400));
        }

        try {
            // Get authenticated user email from SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = getEmailFromAuthentication(authentication);

            // Update lead
            LeadResponseDto response = leadService.updateLead(id, updateLeadRequestDto, userEmail);

            log.info("Lead updated successfully with ID: {}", response.getId());
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Lead updated successfully", 200));

        } catch (NotFoundException e) {
            log.error("Not found exception during lead update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));

        } catch (CustomException e) {
            log.error("Custom exception during lead update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));

        } catch (Exception e) {
            log.error("Unexpected error during lead update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/count")
    @Operation(summary = "Get total leads count", description = "Returns the total number of leads in the system")
    public ResponseEntity<?> getTotalLeadsCount() {
        log.info("Get total leads count request received");

        try {
            Long count = leadService.countTotalLeads();
            log.info("Successfully retrieved total leads count: {}", count);
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(count, "Total leads count retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error counting total leads: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error counting leads: " + e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/status-counts")
    @Operation(summary = "Get lead counts by status", description = "Returns the count of leads grouped by status")
    public ResponseEntity<?> getLeadStatusCounts() {
        log.info("Get lead status counts request received");

        try {
            var statusCounts = leadService.getLeadStatusCounts();
            log.info("Successfully retrieved lead status counts");
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(statusCounts, "Lead status counts retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching lead status counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error fetching status counts: " + e.getMessage(),
                            500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/assign/round-robin")
    @Operation(summary = "Assign leads via round-robin", description = "Distributes unassigned leads evenly across all active counselors in round-robin order")
    public ResponseEntity<?> assignRoundRobin(@RequestBody(required = false) RoundRobinAssignRequestDto requestDto) {
        log.info("Round-robin assignment request received");
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = getEmailFromAuthentication(auth);
            List<Long> leadIds = requestDto != null ? requestDto.getLeadIds() : null;
            var result = roundRobinService.assignLeadsRoundRobin(leadIds, adminEmail);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Round-robin assignment complete", 200));
        } catch (Exception e) {
            log.error("Error during round-robin assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/assign/direct")
    @Operation(summary = "Direct assign leads to one counselor",
               description = "Assigns N unassigned leads (optionally filtered by campaign) all to a single counselor")
    public ResponseEntity<?> directAssignLeads(
            @RequestBody @Valid DirectAssignRequestDto requestDto,
            BindingResult bindingResult) {
        log.info("Direct assign request: campaign='{}', counselorId={}, count={}",
                requestDto.getCampaignName(), requestDto.getCounselorId(), requestDto.getCount());

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = ToMap.bindingResultToMap(bindingResult);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(errors, "Validation failed", 400));
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = getEmailFromAuthentication(auth);
            String sortBy = requestDto.getSortBy() != null ? requestDto.getSortBy() : "createdAt";
            var result = roundRobinService.directAssignLeads(
                    requestDto.getCampaignName(),
                    requestDto.getCounselorId(),
                    requestDto.getCount(),
                    sortBy,
                    adminEmail);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Direct assignment complete", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error during direct assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/counselor-workload")
    @Operation(summary = "Get counselor workload", description = "Returns lead counts per counselor grouped by status")
    public ResponseEntity<?> getCounselorWorkload() {
        log.info("Counselor workload request received");
        try {
            var workload = roundRobinService.getCounselorWorkload();
            return ResponseEntity.ok(new ApiSuccessResponse<>(workload, "Counselor workload fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching counselor workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/reassign")
    @Operation(summary = "Reassign a lead", description = "Reassigns a lead from its current counselor to a new one, logging the change")
    public ResponseEntity<?> reassignLead(
            @PathVariable Long id,
            @RequestBody @Valid ReassignLeadRequestDto requestDto,
            BindingResult bindingResult) {
        log.info("Reassign lead request for ID: {}", id);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = ToMap.bindingResultToMap(bindingResult);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(errors, "Validation failed", 400));
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String byEmail = getEmailFromAuthentication(auth);
            LeadResponseDto response = roundRobinService.reassignLead(id, requestDto.getNewCounselorId(), requestDto.getReason(), byEmail);
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Lead reassigned successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error reassigning lead {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{id}/assignment-history")
    @Operation(summary = "Get lead assignment history", description = "Returns the full reassignment audit trail for a lead")
    public ResponseEntity<?> getAssignmentHistory(@PathVariable Long id) {
        log.info("Assignment history request for lead ID: {}", id);
        try {
            var history = roundRobinService.getLeadAssignmentHistory(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(history, "Assignment history fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching assignment history for lead {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
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

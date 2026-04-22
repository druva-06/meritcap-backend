package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.campaign.CampaignRequestDto;
import com.meritcap.DTOs.responseDTOs.campaign.CampaignStatsDto;
import com.meritcap.exception.CustomException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Slf4j
@RestController
@RequestMapping("campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign Management", description = "APIs for managing lead campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new campaign")
    public ResponseEntity<?> createCampaign(
            @RequestBody @Valid CampaignRequestDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(
                            com.meritcap.utils.ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth != null ? auth.getName() : "unknown";
            CampaignStatsDto result = campaignService.createCampaign(dto, userEmail);
            log.info("Campaign created: {}", result.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(result, "Campaign created successfully", 201));
        } catch (CustomException e) {
            log.error("Campaign creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error creating campaign: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all campaigns with lead stats")
    public ResponseEntity<?> getAllCampaigns() {
        try {
            List<CampaignStatsDto> result = campaignService.getAllCampaignsWithStats();
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Campaigns fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching campaigns: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error fetching campaigns", 500));
        }
    }
}

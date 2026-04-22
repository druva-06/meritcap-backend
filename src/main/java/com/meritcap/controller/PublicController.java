package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.lead.PublicLeadCaptureDto;
import com.meritcap.DTOs.responseDTOs.campaign.PublicCampaignDto;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Unauthenticated endpoints for QR-based lead capture")
public class PublicController {

    private final CampaignService campaignService;

    @GetMapping("/campaigns/{id}")
    @Operation(summary = "Get basic campaign info by ID (public, no auth)")
    public ResponseEntity<?> getCampaign(@PathVariable Long id) {
        try {
            PublicCampaignDto dto = campaignService.getPublicCampaign(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(dto, "Campaign fetched", 200));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching public campaign {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    @PostMapping("/leads/capture")
    @Operation(summary = "Capture a lead from a QR code scan (public, no auth)")
    public ResponseEntity<?> captureLeadFromQR(
            @RequestBody @Valid PublicLeadCaptureDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(
                            com.meritcap.utils.ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            campaignService.captureLeadFromQR(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(null, "Lead captured successfully", 201));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error capturing QR lead: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }
}

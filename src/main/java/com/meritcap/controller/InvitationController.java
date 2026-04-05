package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.invitation.InvitationRequestDto;
import com.meritcap.DTOs.responseDTOs.invitation.InvitationResponseDto;
import com.meritcap.DTOs.responseDTOs.invitation.PagedInvitationResponseDto;
import com.meritcap.DTOs.responseDTOs.invitation.ValidationResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.BadRequestException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.InvitationService;
import com.meritcap.utils.ToMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/invitation")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    /**
     * Create a new user invitation (Admin only)
     * POST /api/invitation/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInvitation(
            @RequestBody @Valid InvitationRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }

        try {
            // Get current user ID from authentication
            Long invitedByUserId = authenticatedUserResolver.resolveCurrentUserId();

            InvitationResponseDto response = invitationService.createInvitation(requestDto, invitedByUserId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Invitation created successfully", 201));

        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error creating invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Validate invitation token (Public endpoint for signup page)
     * GET /api/invitation/validate?token={token}
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateInvitation(@RequestParam String token) {
        try {
            ValidationResponseDto response = invitationService.validateInvitationToken(token);

            if (response.isValid()) {
                return ResponseEntity.ok()
                        .body(new ApiSuccessResponse<>(response, "Invitation is valid", 200));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), response.getMessage(), 400));
            }

        } catch (Exception e) {
            log.error("Error validating invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Get all invitations with pagination and search
     * GET /api/invitation/all?page=0&size=10&search=email
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        try {
            PagedInvitationResponseDto response = invitationService.getAllInvitations(page, size, search);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Invitations retrieved successfully", 200));

        } catch (Exception e) {
            log.error("Error retrieving invitations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Get invitations by status
     * GET /api/invitation/by-status?status=PENDING&page=0&size=10
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getInvitationsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            PagedInvitationResponseDto response = invitationService.getInvitationsByStatus(status, page, size);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Invitations retrieved successfully", 200));

        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error retrieving invitations by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Get invitation by ID
     * GET /api/invitation/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getInvitationById(@PathVariable Long id) {
        try {
            InvitationResponseDto response = invitationService.getInvitationById(id);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Invitation retrieved successfully", 200));

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Resend invitation email
     * POST /api/invitation/resend/{id}
     */
    @PostMapping("/resend/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resendInvitation(@PathVariable Long id) {
        try {
            InvitationResponseDto response = invitationService.resendInvitation(id);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Invitation resent successfully", 200));

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error resending invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Revoke invitation
     * DELETE /api/invitation/revoke/{id}
     */
    @DeleteMapping("/revoke/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeInvitation(@PathVariable Long id) {
        try {
            invitationService.revokeInvitation(id);

            Map<String, Object> response = new HashMap<>();
            response.put("invitationId", id);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response, "Invitation revoked successfully", 200));

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error revoking invitation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }

    /**
     * Cleanup expired invitations (Admin or scheduled job)
     * POST /api/invitation/cleanup-expired
     */
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupExpiredInvitations() {
        try {
            int count = invitationService.markExpiredInvitations();

            Map<String, Object> response = new HashMap<>();
            response.put("expiredCount", count);

            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(response,
                            count + " invitation(s) marked as expired", 200));

        } catch (Exception e) {
            log.error("Error cleaning up expired invitations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Internal server error", 500));
        }
    }
}

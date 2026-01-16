package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.permission.AssignPermissionsRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserPermissionsResponseDto;
import com.consultancy.education.exception.BadRequestException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.PermissionService;
import com.consultancy.education.utils.ToMap;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("user-permissions")
public class UserPermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * Get all permissions for a user (role permissions + additional permissions)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getUserPermissions(@PathVariable Long userId) {
        try {
            UserPermissionsResponseDto responseDto = permissionService.getUserPermissions(userId);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "User permissions retrieved successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving user permissions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving user permissions", 500));
        }
    }

    /**
     * Assign additional permissions to a user (Admin only)
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> assignPermissionsToUser(
            @RequestBody @Valid AssignPermissionsRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            permissionService.assignPermissionsToUser(requestDto);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Permissions assigned successfully to user", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error assigning permissions to user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error assigning permissions", 500));
        }
    }

    /**
     * Revoke additional permissions from a user (Admin only)
     */
    @DeleteMapping("/{userId}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> revokePermissionsFromUser(
            @PathVariable Long userId,
            @RequestBody Set<Long> permissionIds,
            Authentication authentication) {

        try {
            permissionService.revokePermissionsFromUser(userId, permissionIds);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Permissions revoked successfully from user", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error revoking permissions from user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error revoking permissions", 500));
        }
    }

    /**
     * Assign permissions to a role (Admin only)
     */
    @PostMapping("/role/{roleId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds,
            Authentication authentication) {

        try {
            permissionService.assignPermissionsToRole(roleId, permissionIds);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Permissions assigned successfully to role", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error assigning permissions to role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error assigning permissions to role", 500));
        }
    }

    /**
     * Revoke permissions from a role (Admin only)
     */
    @DeleteMapping("/role/{roleId}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> revokePermissionsFromRole(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds,
            Authentication authentication) {

        try {
            permissionService.revokePermissionsFromRole(roleId, permissionIds);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Permissions revoked successfully from role", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error revoking permissions from role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error revoking permissions from role", 500));
        }
    }
}

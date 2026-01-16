package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.permission.PermissionRequestDto;
import com.consultancy.education.DTOs.responseDTOs.permission.PermissionResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
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
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * Create a new permission (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> createPermission(
            @RequestBody @Valid PermissionRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            PermissionResponseDto responseDto = permissionService.createPermission(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(responseDto, "Permission created successfully", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error creating permission: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error creating permission", 500));
        }
    }

    /**
     * Update an existing permission (Admin only)
     */
    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody @Valid PermissionRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            PermissionResponseDto responseDto = permissionService.updatePermission(permissionId, requestDto);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Permission updated successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error updating permission: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error updating permission", 500));
        }
    }

    /**
     * Delete a permission (Admin only)
     */
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deletePermission(
            @PathVariable Long permissionId,
            Authentication authentication) {

        try {
            permissionService.deletePermission(permissionId);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Permission deleted successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting permission: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error deleting permission", 500));
        }
    }

    /**
     * Get a permission by ID
     */
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getPermissionById(@PathVariable Long permissionId) {
        try {
            PermissionResponseDto responseDto = permissionService.getPermissionById(permissionId);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Permission retrieved successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving permission: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving permission", 500));
        }
    }

    /**
     * Get a permission by name
     */
    @GetMapping("/name/{permissionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getPermissionByName(@PathVariable String permissionName) {
        try {
            PermissionResponseDto responseDto = permissionService.getPermissionByName(permissionName);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Permission retrieved successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving permission: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving permission", 500));
        }
    }

    /**
     * Get all permissions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getAllPermissions() {
        try {
            List<PermissionResponseDto> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(permissions, "Permissions retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving permissions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving permissions", 500));
        }
    }

    /**
     * Get all active permissions
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getActivePermissions() {
        try {
            List<PermissionResponseDto> permissions = permissionService.getActivePermissions();
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(permissions, "Active permissions retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving active permissions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving active permissions", 500));
        }
    }

    /**
     * Get permissions by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getPermissionsByCategory(@PathVariable String category) {
        try {
            List<PermissionResponseDto> permissions = permissionService.getPermissionsByCategory(category);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(permissions, "Permissions retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving permissions by category: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving permissions", 500));
        }
    }

    /**
     * Get permissions by role ID
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getPermissionsByRoleId(@PathVariable Long roleId) {
        try {
            Set<PermissionResponseDto> permissions = permissionService.getPermissionsByRoleId(roleId);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(permissions, "Role permissions retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving permissions for role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving role permissions", 500));
        }
    }
}

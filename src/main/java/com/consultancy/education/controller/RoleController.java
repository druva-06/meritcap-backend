package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.role.RoleRequestDto;
import com.consultancy.education.DTOs.responseDTOs.role.RoleResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.BadRequestException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.RoleService;
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

@Slf4j
@RestController
@RequestMapping("roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Create a new role (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> createRole(
            @RequestBody @Valid RoleRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            // TODO: Get actual user ID from authentication
            Long createdBy = 1L; // Placeholder
            RoleResponseDto responseDto = roleService.createRole(requestDto, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(responseDto, "Role created successfully", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error creating role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error creating role", 500));
        }
    }

    /**
     * Update an existing role (Admin only)
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateRole(
            @PathVariable Long roleId,
            @RequestBody @Valid RoleRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),
                            "Validation failed", 400));
        }

        try {
            // TODO: Get actual user ID from authentication
            Long updatedBy = 1L; // Placeholder
            RoleResponseDto responseDto = roleService.updateRole(roleId, requestDto, updatedBy);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Role updated successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException | AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error updating role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error updating role", 500));
        }
    }

    /**
     * Delete a role (Admin only)
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deleteRole(
            @PathVariable Long roleId,
            Authentication authentication) {

        try {
            // TODO: Get actual user ID from authentication
            Long deletedBy = 1L; // Placeholder
            roleService.deleteRole(roleId, deletedBy);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(null, "Role deleted successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error deleting role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error deleting role", 500));
        }
    }

    /**
     * Get a role by ID
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getRoleById(@PathVariable Long roleId) {
        try {
            RoleResponseDto responseDto = roleService.getRoleById(roleId);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Role retrieved successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving role", 500));
        }
    }

    /**
     * Get a role by name
     */
    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getRoleByName(@PathVariable String roleName) {
        try {
            RoleResponseDto responseDto = roleService.getRoleByName(roleName);
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(responseDto, "Role retrieved successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error retrieving role: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving role", 500));
        }
    }

    /**
     * Get all roles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<RoleResponseDto> roles = roleService.getAllRoles();
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(roles, "Roles retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving roles: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving roles", 500));
        }
    }

    /**
     * Get all active roles
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getActiveRoles() {
        try {
            List<RoleResponseDto> roles = roleService.getActiveRoles();
            return ResponseEntity.ok()
                    .body(new ApiSuccessResponse<>(roles, "Active roles retrieved successfully", 200));
        } catch (Exception e) {
            log.error("Error retrieving active roles: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), "Error retrieving active roles", 500));
        }
    }
}

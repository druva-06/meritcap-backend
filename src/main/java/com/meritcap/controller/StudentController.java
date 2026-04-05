package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.student.StudentProfileRequestDto;
import com.meritcap.DTOs.requestDTOs.student.StudentProfileUpdateRequestDto;
import com.meritcap.DTOs.responseDTOs.student.StudentProfileResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.exception.ValidationException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.StudentProfileService;
import com.meritcap.utils.ToMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/student/profile")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProfileService studentProfileService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    // Create/Add Profile (Student Self)
    @PostMapping("/add")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> addProfile(@RequestBody @Valid StudentProfileRequestDto requestDto, BindingResult bindingResult) {
        log.info("Add profile request received: {}", requestDto);
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in add profile: {}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
            if (requestDto.getUserId() != null && !currentUserId.equals(requestDto.getUserId())) {
                log.warn("Ignoring mismatched add-profile userId {} for authenticated user {}", requestDto.getUserId(),
                        currentUserId);
            }
            requestDto.setUserId(currentUserId);
            StudentProfileResponseDto responseDto = studentProfileService.addProfile(requestDto);
            log.info("Profile added for userId: {}", responseDto.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(responseDto, "Profile created successfully", 201));
        } catch (AlreadyExistException e) {
            log.warn("Profile already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error adding profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    // Get Own Profile (Student Self)
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getOwnProfile(@RequestParam(required = false) Long userId) {
        log.info("Get own profile request for userId: {}", userId);
        try {
            Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
            if (userId != null && !currentUserId.equals(userId)) {
                log.warn("Ignoring mismatched profile userId {} for authenticated user {}", userId, currentUserId);
            }
            StudentProfileResponseDto responseDto = studentProfileService.getProfileByUserId(currentUserId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(responseDto, "Profile fetched successfully", 200));
        } catch (NotFoundException e) {
            log.warn("Profile not found for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error fetching profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    // Update Own Profile (Student Self)
    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid StudentProfileUpdateRequestDto requestDto,
            BindingResult bindingResult, @RequestParam(required = false) Long userId) {
        log.info("Update profile request for userId: {}, data: {}", userId, requestDto);
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in update profile: {}", bindingResult.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
            if (userId != null && !currentUserId.equals(userId)) {
                log.warn("Ignoring mismatched update-profile userId {} for authenticated user {}", userId,
                        currentUserId);
            }
            StudentProfileResponseDto responseDto = studentProfileService.updateProfile(currentUserId, requestDto);
            log.info("Profile updated for userId: {}", currentUserId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(responseDto, "Profile updated successfully", 200));
        } catch (ValidationException e) {
            log.warn("Validation failed in update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 400));
        } catch (NotFoundException e) {
            log.warn("Profile not found for update userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

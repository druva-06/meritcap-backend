package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.meritcap.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.StudentEducationService;
import com.meritcap.utils.ToMap;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/student-education")
public class StudentEducationController {

    private final StudentEducationService studentEducationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public StudentEducationController(StudentEducationService studentEducationService,
                                      AuthenticatedUserResolver authenticatedUserResolver) {
        this.studentEducationService = studentEducationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> addStudentEducation(@RequestBody @Valid StudentEducationRequestDto dto,
            BindingResult bindingResult, @RequestParam(required = false) Long userId) {
        log.info("Add student education request received: userId={}, payload={}", userId, dto);
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in addStudentEducation: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
            if (userId != null && !currentUserId.equals(userId)) {
                log.warn("Ignoring mismatched add-education userId {} for authenticated user {}", userId, currentUserId);
            }
            StudentEducationResponseDto response = studentEducationService.addStudentEducation(dto, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(response, "Student education added successfully", 201));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error in addStudentEducation", e);
            return ResponseEntity.internalServerError().body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateStudentEducation(@RequestBody @Valid StudentEducationRequestDto dto, @RequestParam Long educationId, BindingResult bindingResult) {
        log.info("Update student education request: educationId={}, payload={}", educationId, dto);
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in updateStudentEducation: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            StudentEducationResponseDto response = studentEducationService.updateStudentEducationForCurrentUser(dto, educationId, authenticatedUserResolver.resolveCurrentUserId());
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Student education updated successfully", 200));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error in updateStudentEducation", e);
            return ResponseEntity.internalServerError().body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> deleteStudentEducation(@RequestParam Long educationId) {
        log.info("Delete student education request: educationId={}", educationId);
        try {
            studentEducationService.deleteStudentEducationForCurrentUser(educationId, authenticatedUserResolver.resolveCurrentUserId());
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Student education deleted successfully", 200));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error in deleteStudentEducation", e);
            return ResponseEntity.internalServerError().body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/get")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentEducation(@RequestParam(required = false) Long userId) {
        log.info("Get student education request: userId={}", userId);
        try {
            Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
            if (userId != null && !currentUserId.equals(userId)) {
                log.warn("Ignoring mismatched get-education userId {} for authenticated user {}", userId, currentUserId);
            }
            List<StudentEducationResponseDto> result = studentEducationService.getStudentEducation(currentUserId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(result, "Student education fetched successfully", 200));
        } catch (com.meritcap.exception.CustomException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error in getStudentEducation", e);
            return ResponseEntity.internalServerError().body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

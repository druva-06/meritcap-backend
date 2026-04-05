package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationEditRequestDto;
import com.meritcap.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationRequestDto;
import com.meritcap.DTOs.responseDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.StudentCollegeCourseRegistrationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/student-college-course-registration")
@RestController
@Slf4j
public class StudentCollegeCourseRegistrationController {

    private final StudentCollegeCourseRegistrationService studentCollegeCourseRegistrationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    StudentCollegeCourseRegistrationController(StudentCollegeCourseRegistrationService studentCollegeCourseRegistrationService,
            AuthenticatedUserResolver authenticatedUserResolver) {
        this.studentCollegeCourseRegistrationService = studentCollegeCourseRegistrationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/start")
    public ResponseEntity<?> registerStudent(@RequestBody @Valid StudentCollegeCourseRegistrationRequestDto request) {
        log.info("New registration: studentId={}, courseId={}, intake={}",
                request.getStudentId(), request.getCollegeCourseId(), request.getIntakeSession());
        try {
            StudentCollegeCourseRegistrationResponseDto response = studentCollegeCourseRegistrationService
                    .registerStudentForCourseForCurrentUser(request, authenticatedUserResolver.resolveCurrentUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Registration created", 201));
        } catch (AlreadyExistException e) {
            log.warn("Duplicate registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (CustomException e) {
            log.warn("Forbidden registration create access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (NotFoundException e) {
            log.warn("Not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/edit")
    public ResponseEntity<?> editRegistration(@RequestBody @Valid StudentCollegeCourseRegistrationEditRequestDto requestDto) {
        log.info("PUT /edit called for registrationId={}", requestDto.getRegistrationId());
        try {
            StudentCollegeCourseRegistrationResponseDto response =
                    studentCollegeCourseRegistrationService.editRegistrationForCurrentUser(
                            requestDto, authenticatedUserResolver.resolveCurrentUserId());
            log.info("Registration edited successfully, id={}", response.getRegistrationId());
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Registration updated", 200));
        } catch (NotFoundException e) {
            log.warn("Edit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (CustomException e) {
            log.warn("Forbidden registration edit access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (IllegalStateException e) {
            log.warn("Invalid edit attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Edit registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{registrationId}")
    public ResponseEntity<?> getRegistration(@PathVariable Long registrationId) {
        log.info("GET /{} called", registrationId);
        try {
            StudentCollegeCourseRegistrationResponseDto response =
                    studentCollegeCourseRegistrationService.getRegistrationByIdForCurrentUser(
                            registrationId, authenticatedUserResolver.resolveCurrentUserId());
            log.info("Fetched registrationId={} successfully", registrationId);
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(response, "Registration details fetched", 200)
            );
        } catch (NotFoundException e) {
            log.warn("Registration not found: {}", registrationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (CustomException e) {
            log.warn("Forbidden registration access for id={}: {}", registrationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error fetching registrationId={}", registrationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getRegistrationsByStudent(@PathVariable Long studentId) {
        log.info("GET /student/{} called", studentId);
        try {
            List<StudentCollegeCourseRegistrationResponseDto> responseList =
                    studentCollegeCourseRegistrationService.getRegistrationsByStudentIdForCurrentUser(
                            studentId, authenticatedUserResolver.resolveCurrentUserId());
            log.info("Fetched {} registrations for studentId={}", responseList.size(), studentId);
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(responseList, "Registrations fetched", 200)
            );
        } catch (CustomException e) {
            log.warn("Forbidden registration list access for studentId={}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Error fetching registrations for studentId={}", studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }
}

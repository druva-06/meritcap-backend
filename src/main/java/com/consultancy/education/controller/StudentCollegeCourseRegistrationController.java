package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationEditRequestDto;
import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.StudentCollegeCourseRegistrationService;
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

    StudentCollegeCourseRegistrationController(StudentCollegeCourseRegistrationService studentCollegeCourseRegistrationService) {
        this.studentCollegeCourseRegistrationService = studentCollegeCourseRegistrationService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/start")
    public ResponseEntity<?> registerStudent(@RequestBody @Valid StudentCollegeCourseRegistrationRequestDto request) {
        log.info("New registration: studentId={}, courseId={}, intake={}",
                request.getStudentId(), request.getCollegeCourseId(), request.getIntakeSession());
        try {
            StudentCollegeCourseRegistrationResponseDto response = studentCollegeCourseRegistrationService.registerStudentForCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Registration created", 201));
        } catch (AlreadyExistException e) {
            log.warn("Duplicate registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
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

    @PutMapping("/edit")
    public ResponseEntity<?> editRegistration(@RequestBody @Valid StudentCollegeCourseRegistrationEditRequestDto requestDto) {
        log.info("PUT /edit called for registrationId={}", requestDto.getRegistrationId());
        try {
            StudentCollegeCourseRegistrationResponseDto response =
                    studentCollegeCourseRegistrationService.editRegistration(requestDto);
            log.info("Registration edited successfully, id={}", response.getRegistrationId());
            return ResponseEntity.ok(new ApiSuccessResponse<>(response, "Registration updated", 200));
        } catch (NotFoundException e) {
            log.warn("Edit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
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
                    studentCollegeCourseRegistrationService.getRegistrationById(registrationId);
            log.info("Fetched registrationId={} successfully", registrationId);
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(response, "Registration details fetched", 200)
            );
        } catch (NotFoundException e) {
            log.warn("Registration not found: {}", registrationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
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
                    studentCollegeCourseRegistrationService.getRegistrationsByStudentId(studentId);
            log.info("Fetched {} registrations for studentId={}", responseList.size(), studentId);
            return ResponseEntity.ok(
                    new ApiSuccessResponse<>(responseList, "Registrations fetched", 200)
            );
        } catch (Exception e) {
            log.error("Error fetching registrations for studentId={}", studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }
}

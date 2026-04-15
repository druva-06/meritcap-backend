package com.meritcap.controller;

import com.meritcap.DTOs.responseDTOs.student.StudentSummaryResponseDto;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.AdminStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/{studentId}/summary")
    public ResponseEntity<?> getStudentSummary(@PathVariable Long studentId) {
        log.info("Admin: GET /admin/students/{}/summary called", studentId);
        try {
            StudentSummaryResponseDto summary = adminStudentService.getStudentSummary(studentId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(summary, "Student summary fetched successfully", 200));
        } catch (NotFoundException e) {
            log.warn("Admin: Student not found, id={}", studentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Admin: Error fetching student summary for id={}", studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }
}

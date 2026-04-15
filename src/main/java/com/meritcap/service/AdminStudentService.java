package com.meritcap.service;

import com.meritcap.DTOs.responseDTOs.student.StudentSummaryResponseDto;

public interface AdminStudentService {
    StudentSummaryResponseDto getStudentSummary(Long studentId);
}

package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.meritcap.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;

import java.util.List;

public interface StudentEducationService {
    StudentEducationResponseDto addStudentEducation(StudentEducationRequestDto dto, Long userId);
    StudentEducationResponseDto updateStudentEducation(StudentEducationRequestDto dto, Long educationId);
    StudentEducationResponseDto updateStudentEducationForCurrentUser(StudentEducationRequestDto dto, Long educationId, Long currentUserId);
    void deleteStudentEducation(Long educationId);
    void deleteStudentEducationForCurrentUser(Long educationId, Long currentUserId);
    List<StudentEducationResponseDto> getStudentEducation(Long userId);
    void attachCertificate(Long educationId, Long documentId);
}

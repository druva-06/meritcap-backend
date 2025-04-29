package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface StudentEducationService {

    StudentEducationResponseDto addStudentEducation(StudentEducationRequestDto studentEducationRequestDto, Long userId);

    StudentEducationResponseDto updateStudentEducation(StudentEducationRequestDto studentEducationRequestDto, Long studentEducationId);

    List<StudentEducationResponseDto> getStudentEducation(Long userId);
}

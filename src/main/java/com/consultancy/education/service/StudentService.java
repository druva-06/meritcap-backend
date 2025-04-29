package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.student.StudentRequestDto;
import com.consultancy.education.DTOs.requestDTOs.student.StudentUpdateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.student.StudentResponseDto;

public interface StudentService {
    StudentResponseDto addStudent(StudentRequestDto studentRequestDto) throws Exception;

    StudentResponseDto updateStudent(StudentUpdateRequestDto studentUpdateRequestDto, Long studentId);

    StudentResponseDto getStudent(Long userId);
}

package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationEditRequestDto;
import com.meritcap.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationRequestDto;
import com.meritcap.DTOs.responseDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationResponseDto;

import java.util.List;

public interface StudentCollegeCourseRegistrationService {

    StudentCollegeCourseRegistrationResponseDto registerStudentForCourse(StudentCollegeCourseRegistrationRequestDto request);

    StudentCollegeCourseRegistrationResponseDto registerStudentForCourseForCurrentUser(
            StudentCollegeCourseRegistrationRequestDto request, Long currentUserId);

    StudentCollegeCourseRegistrationResponseDto editRegistration(StudentCollegeCourseRegistrationEditRequestDto requestDto);

    StudentCollegeCourseRegistrationResponseDto editRegistrationForCurrentUser(
            StudentCollegeCourseRegistrationEditRequestDto requestDto, Long currentUserId);

    StudentCollegeCourseRegistrationResponseDto getRegistrationById(Long registrationId);

    StudentCollegeCourseRegistrationResponseDto getRegistrationByIdForCurrentUser(Long registrationId, Long currentUserId);

    List<StudentCollegeCourseRegistrationResponseDto> getRegistrationsByStudentId(Long studentId);

    List<StudentCollegeCourseRegistrationResponseDto> getRegistrationsByStudentIdForCurrentUser(Long studentId,
            Long currentUserId);

    StudentCollegeCourseRegistrationResponseDto submitRegistration(Long registrationId);
}

package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.student.StudentRequestDto;
import com.consultancy.education.DTOs.requestDTOs.student.StudentUpdateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.student.StudentResponseDto;
import com.consultancy.education.enums.ActiveStatus;
import com.consultancy.education.model.Student;

public class StudentTransformer {

    public static void updateEntity(Student student, StudentRequestDto studentRequestDto) {
        student.setAlternatePhoneNumber(studentRequestDto.getAlternatePhoneNumber());
        student.setGender(studentRequestDto.getGender());
        student.setDateOfBirth(studentRequestDto.getDateOfBirth());
        student.setGraduationLevel(studentRequestDto.getGraduationLevel());
        student.setProfileActiveStatus(ActiveStatus.ACTIVE);
    }

    public static StudentResponseDto toResDTO(Student student) {
        return StudentResponseDto.builder()
                .userId(student.getUser().getId())
                .graduationLevel(student.getGraduationLevel())
                .dateOfBirth(student.getDateOfBirth())
                .alternatePhoneNumber(student.getAlternatePhoneNumber())
                .gender(student.getGender())
                .profileActiveStatus(student.getProfileActiveStatus())
                .profileCompletion(student.getProfileCompletion())
                .aadhaarCardFile(student.getAadhaarCardFile())
                .birthCertificateFile(student.getBirthCertificateFile())
                .panCardFile(student.getPanCardFile())
                .passportFile(student.getPassportFile())
                .build();
    }

//    public static void updateStudentDetails(Student student, StudentUpdateRequestDto studentRequestDto) {
//        student.setUsername(studentRequestDto.getUsername());
//        student.setEmail(studentRequestDto.getEmail());
//        student.setFirstName(studentRequestDto.getFirstName());
//        student.setLastName(studentRequestDto.getLastName());
//        student.setPhoneNumber(studentRequestDto.getPhoneNumber());
//        student.setAlternatePhoneNumber(studentRequestDto.getAlternatePhoneNumber());
//        student.setDateOfBirth(studentRequestDto.getBirthDate());
//        student.setGender(studentRequestDto.getGender());
//        student.setGraduationLevel(studentRequestDto.getGraduationLevel());
//        student.setProfileActiveStatus(studentRequestDto.getProfileActiveStatus());
//        student.setProfileCompletion(studentRequestDto.getProfileCompletion());
//        student.setProfileImage(studentRequestDto.getProfileImage());
//        student.setAadhaarNumber(studentRequestDto.getAadhaarNumber());
//        student.setAadhaarCardFile(studentRequestDto.getAadhaarCardFile());
//        student.setPassportNumber(studentRequestDto.getPassportNumber());
//        student.setPassportFile(studentRequestDto.getPassportFile());
//    }
}

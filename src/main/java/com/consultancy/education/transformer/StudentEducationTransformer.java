package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.StudentEducation;

import java.util.ArrayList;
import java.util.List;

public class StudentEducationTransformer {

    public static StudentEducation toEntity(StudentEducationRequestDto studentEducationRequestDto) {
        return StudentEducation.builder()
                .educationLevel(studentEducationRequestDto.getEducationLevel())
                .institutionName(studentEducationRequestDto.getInstitutionName())
                .board(studentEducationRequestDto.getBoard())
                .collegeCode(studentEducationRequestDto.getCollegeCode())
                .institutionAddress(studentEducationRequestDto.getInstitutionAddress())
                .startYear(studentEducationRequestDto.getStartYear())
                .endYear(studentEducationRequestDto.getEndYear())
                .percentage(studentEducationRequestDto.getPercentage())
                .cgpa(studentEducationRequestDto.getCgpa())
                .division(studentEducationRequestDto.getDivision())
                .specialization(studentEducationRequestDto.getSpecialization())
                .backlogs(studentEducationRequestDto.getBacklogs())
                .certificate(studentEducationRequestDto.getCertificate())
                .build();
    }

    public static StudentEducationResponseDto toResDTO(StudentEducation studentEducation, Student student) {

        return StudentEducationResponseDto.builder()
                .userId(student.getUser().getId())
                .educationId(studentEducation.getId())
                .educationLevel(studentEducation.getEducationLevel())
                .institutionName(studentEducation.getInstitutionName())
                .board(studentEducation.getBoard())
                .collegeCode(studentEducation.getCollegeCode())
                .institutionAddress(studentEducation.getInstitutionAddress())
                .startYear(studentEducation.getStartYear())
                .endYear(studentEducation.getEndYear())
                .percentage(studentEducation.getPercentage())
                .cgpa(studentEducation.getCgpa())
                .division(studentEducation.getDivision())
                .specialization(studentEducation.getSpecialization())
                .backlogs(studentEducation.getBacklogs())
                .certificate(studentEducation.getCertificate())
                .build();
    }

    public static List<StudentEducationResponseDto> toResDTO(List<StudentEducation> studentEducation, Student student) {

        List<StudentEducationResponseDto> studentEducationResponseDtos = new ArrayList<>();

        for (StudentEducation studentEducationDto : studentEducation) {
            studentEducationResponseDtos.add(toResDTO(studentEducationDto, student));
        }

        return studentEducationResponseDtos;
    }

    public static void updateStudentEducation(StudentEducation studentEducation, StudentEducationRequestDto studentEducationRequestDto) {
        studentEducation.setEducationLevel(studentEducationRequestDto.getEducationLevel());
        studentEducation.setInstitutionName(studentEducationRequestDto.getInstitutionName());
        studentEducation.setBoard(studentEducationRequestDto.getBoard());
        studentEducation.setCollegeCode(studentEducationRequestDto.getCollegeCode());
        studentEducation.setInstitutionAddress(studentEducationRequestDto.getInstitutionAddress());
        studentEducation.setStartYear(studentEducationRequestDto.getStartYear());
        studentEducation.setEndYear(studentEducationRequestDto.getEndYear());
        studentEducation.setPercentage(studentEducationRequestDto.getPercentage());
        studentEducation.setCgpa(studentEducationRequestDto.getCgpa());
        studentEducation.setDivision(studentEducationRequestDto.getDivision());
        studentEducation.setSpecialization(studentEducationRequestDto.getSpecialization());
        studentEducation.setBacklogs(studentEducationRequestDto.getBacklogs());
        studentEducation.setCertificate(studentEducationRequestDto.getCertificate());
    }
}

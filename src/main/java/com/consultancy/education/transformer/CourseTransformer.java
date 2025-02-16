package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.course.CourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.course.CourseResponseDto;
import com.consultancy.education.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseTransformer {

    public static Course toEntity(CourseRequestDto courseRequestDto) {
        return Course.builder()
                .name(courseRequestDto.getName())
                .department(courseRequestDto.getDepartment())
                .graduationLevel(courseRequestDto.getGraduationLevel())
                .specialization(courseRequestDto.getSpecialization())
                .build();
    }

    public static CourseResponseDto toResDTO(Course course) {
        return CourseResponseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .department(course.getDepartment())
                .graduationLevel(course.getGraduationLevel())
                .specialization(course.getSpecialization())
                .build();
    }

    public static List<CourseResponseDto> toResDTO(List<Course> courses) {
        List<CourseResponseDto> courseResponseDtos = new ArrayList<>();
        for (Course course : courses) {
            courseResponseDtos.add(toResDTO(course));
        }
        return courseResponseDtos;
    }

    public static void updateCourse(Course course, CourseRequestDto courseRequestDto) {
        course.setName(courseRequestDto.getName());
        course.setDepartment(courseRequestDto.getDepartment());
        course.setGraduationLevel(courseRequestDto.getGraduationLevel());
        course.setSpecialization(courseRequestDto.getSpecialization());
    }

    public static void updateCourseDetailsEntityToEntity(Course existingCourse, Course course) {
        existingCourse.setSpecialization(course.getSpecialization());
    }
}

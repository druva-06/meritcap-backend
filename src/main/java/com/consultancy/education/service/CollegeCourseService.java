package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.search.SearchCourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.consultancy.education.DTOs.responseDTOs.search.SearchCourseResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface CollegeCourseService {
    String bulkCollegeCourseUpload(MultipartFile file);

    SearchCourseResponseDto<CollegeCourseResponseDto> getCollegeCourses(SearchCourseRequestDto searchCourseRequestDto);

//    CollegeCourseResponseDto addCollegeCourse(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto, Long collegeId, Long courseId);
//
//    CollegeCourseResponseDto updateCollegeCourse(@Valid CollegeCourseRequestExcelDto collegeCourseRequestExcelDto, Long collegeCourseId);
//
//    CollegeCourseResponseDto deleteCollegeCourse(Long collegeCourseId);
}

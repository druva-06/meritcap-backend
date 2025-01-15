package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.consultancy.education.DTOs.requestDTOs.search.SearchCourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.consultancy.education.DTOs.responseDTOs.search.SearchCourseResponseDto;
import com.consultancy.education.enums.GraduationLevel;
import com.consultancy.education.helper.ExcelHelper;
import com.consultancy.education.model.College;
import com.consultancy.education.model.CollegeCourse;
import com.consultancy.education.model.Course;
import com.consultancy.education.repository.CollegeCourseRepository;
import com.consultancy.education.repository.CollegeRepository;
import com.consultancy.education.repository.CourseRepository;
import com.consultancy.education.service.CollegeCourseService;
import com.consultancy.education.transformer.CollegeCourseTransformer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollegeCourseServiceImpl implements CollegeCourseService {

    private final CollegeCourseRepository collegeCourseRepository;
    private final CollegeRepository collegeRepository;
    private final CourseRepository courseRepository;

    public CollegeCourseServiceImpl(CollegeCourseRepository collegeCourseRepository,
                                    CollegeRepository collegeRepository,
                                    CourseRepository courseRepository) {
        this.collegeCourseRepository = collegeCourseRepository;
        this.collegeRepository = collegeRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public String bulkCollegeCourseUpload(MultipartFile file) {
        int mapped = 0;
        int skipped = 0;
        ArrayList<Long> duplicates = new ArrayList<>();
        try {
            List<CollegeCourseRequestExcelDto> collegeCourseRequestExcelDtos = ExcelHelper.convertCollegeCourseExcelIntoList(file.getInputStream());
            for(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto: collegeCourseRequestExcelDtos) {
                GraduationLevel graduationLevel;
                try{
                    graduationLevel = GraduationLevel.valueOf(collegeCourseRequestExcelDto.getGraduationLevel().toUpperCase());
                }
                catch(Exception e){
                    skipped++;
                    continue;
                }
                College college = collegeRepository.findByNameAndCampusAndCountry(collegeCourseRequestExcelDto.getCollegeName(), collegeCourseRequestExcelDto.getCampus(), collegeCourseRequestExcelDto.getCountry());
                List<Course> courses = courseRepository.findByNameAndGraduationLevel(collegeCourseRequestExcelDto.getCourseName(), graduationLevel);
                if(courses.size() > 1){
                   for(int i=0; i<courses.size(); i++){
                       duplicates.add(courses.get(i).getId());
                   }
                }
                if(college != null && courses.get(0) != null) {
                    CollegeCourse collegeCourse = CollegeCourseTransformer.excelToEntity(collegeCourseRequestExcelDto);
                    collegeCourse.setCollege(college);
                    System.out.println(collegeCourse.getIntakeMonths());
                    collegeCourse.setCourse(courses.get(0));
                    System.out.println("Count " + mapped + " College Name: " + college.getName() + " Course Name: " + courses.get(0).getName());
                    courses.get(0).getCollegeCourses().add(collegeCourse);
                    college.getCollegeCourses().add(collegeCourse);
                    collegeRepository.save(college);
                    mapped++;
                }
                else{
                    skipped++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Duplicated courses Ids " + duplicates);
        return "Total courses mapped: " + mapped + " skipped: " + skipped;
    }

    @Override
    public SearchCourseResponseDto<CollegeCourseResponseDto> getCollegeCourses(SearchCourseRequestDto searchCourseRequestDto) {
        return collegeCourseRepository.searchCollegeCourses(searchCourseRequestDto);
    }

//    @Override
//    public CollegeCourseResponseDto addCollegeCourse(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto, Long collegeId, Long courseId) {
//
//        if(collegeRepository.findById(collegeId).isEmpty()) {
//            throw new NotFoundException("College not found");
//        }
//        if(courseRepository.findById(courseId).isEmpty()) {
//            throw new NotFoundException("Course not found");
//        }
//
//        College college = collegeRepository.findById(collegeId).get();
//        Course course = courseRepository.findById(courseId).get();
//        CollegeCourse collegeCourse = CollegeCourseTransformer.toEntity(collegeCourseRequestExcelDto);
//
//        collegeCourse.setCourse(course);
//        collegeCourse.setCollege(college);
//
//        college.getCollegeCourses().add(collegeCourse);
//        course.getCollegeCourses().add(collegeCourse);
//
//        collegeCourse = collegeCourseRepository.save(collegeCourse);
//
//        return CollegeCourseTransformer.toResDto(collegeCourse, collegeCourse.getId(), college.getName(), course.getName());
//    }
//
//    @Override
//    public CollegeCourseResponseDto updateCollegeCourse(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto, Long collegeCourseId) {
//
//        if(collegeCourseRepository.findById(collegeCourseId).isEmpty()) {
//            throw new NotFoundException("College Course not found");
//        }
//
//        CollegeCourse collegeCourse = collegeCourseRepository.findById(collegeCourseId).get();
//        CollegeCourseTransformer.updateCollegeCourse(collegeCourse, collegeCourseRequestExcelDto);
//        collegeCourse = collegeCourseRepository.save(collegeCourse);
//        return CollegeCourseTransformer.toResDto(collegeCourse, collegeCourseId, collegeCourse.getCollege().getName(), collegeCourse.getCourse().getName());
//    }
//
//    @Override
//    public CollegeCourseResponseDto deleteCollegeCourse(Long collegeCourseId) {
//
//        if(collegeCourseRepository.findById(collegeCourseId).isEmpty()) {
//            throw new NotFoundException("College Course not found");
//        }
//        CollegeCourse collegeCourse = collegeCourseRepository.findById(collegeCourseId).get();
//        String collegeName = collegeCourse.getCollege().getName();
//        String courseName = collegeCourse.getCourse().getName();
//        collegeCourseRepository.delete(collegeCourse);
//        return CollegeCourseTransformer.toResDto(collegeCourse, collegeCourseId, collegeName, courseName);
//    }
}

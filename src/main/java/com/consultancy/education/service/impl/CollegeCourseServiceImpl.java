package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.consultancy.education.DTOs.requestDTOs.search.SearchCourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.consultancy.education.DTOs.responseDTOs.currency.CurrencyResponseDTO;
import com.consultancy.education.DTOs.responseDTOs.search.SearchCourseResponseDto;
import com.consultancy.education.api.CurrencyAPIService;
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
import com.consultancy.education.utils.FormatConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CollegeCourseServiceImpl implements CollegeCourseService {


    @PersistenceContext
    EntityManager entityManager;

    private final CollegeCourseRepository collegeCourseRepository;
    private final CollegeRepository collegeRepository;
    private final CourseRepository courseRepository;

    public CollegeCourseServiceImpl(CollegeCourseRepository collegeCourseRepository,
                                    CollegeRepository collegeRepository,
                                    CourseRepository courseRepository,
                                    CurrencyAPIService currencyAPIService) {
        this.collegeCourseRepository = collegeCourseRepository;
        this.collegeRepository = collegeRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public String bulkCollegeCourseUpload(MultipartFile file) {
        try {
            // Step 1: Read data from Excel
            List<CollegeCourseRequestExcelDto> courseDtos = ExcelHelper.convertCollegeCourseExcelIntoList(file.getInputStream());

            if (courseDtos.isEmpty()) {
                return "No courses to upload";
            }

            // Step 2: Fetch existing Colleges and Courses to avoid multiple DB queries
            Set<String> campusCodes = courseDtos.stream().map(CollegeCourseRequestExcelDto::getCampusCode).collect(Collectors.toSet());
            Set<String> courseKeys = courseDtos.stream()
                    .map(dto -> dto.getCourseName() + "|" + dto.getDepartment() + "|" + dto.getGraduationLevel())
                    .collect(Collectors.toSet());

            Map<String, College> collegeMap = collegeRepository.findByCampusCodeIn(campusCodes)
                    .stream().collect(Collectors.toMap(College::getCampusCode, c -> c));

            Map<String, Course> courseMap = courseRepository.findByCourseKeys(courseKeys)
                    .stream().collect(Collectors.toMap(
                            course -> course.getName() + "|" + course.getDepartment() + "|" + course.getGraduationLevel(),
                            c -> c));

            // Step 3: Prepare entities for batch insert
            List<CollegeCourse> collegeCourses = new ArrayList<>();

            for (CollegeCourseRequestExcelDto dto : courseDtos) {
                College college = collegeMap.get(dto.getCampusCode());
                Course course = courseMap.get(dto.getCourseName() + "|" + dto.getDepartment() + "|" + dto.getGraduationLevel().toUpperCase());

                if (college != null && course != null) {
                    CollegeCourse newCourse = new CollegeCourse();
                    newCourse.setCollege(college);
                    newCourse.setCourse(course);
                    newCourse.setCourseUrl(dto.getCourseUrl());
                    newCourse.setDuration(FormatConverter.cnvrtDurationToInteger(dto.getDuration()));
                    newCourse.setIntakeMonths(FormatConverter.cnvrtIntakesToList(dto.getIntakeMonths()));
                    newCourse.setIntakeYear(dto.getIntakeYear());
                    newCourse.setEligibilityCriteria(dto.getEligibilityCriteria());
                    newCourse.setApplicationFee(dto.getApplicationFee());
                    newCourse.setTuitionFee(dto.getTuitionFee());
                    newCourse.setIeltsMinScore(dto.getIeltsMinScore());
                    newCourse.setIeltsMinBandScore(dto.getIeltsMinBandScore());
                    newCourse.setToeflMinScore(dto.getToeflMinScore());
                    newCourse.setToeflMinBandScore(dto.getToeflMinBandScore());
                    newCourse.setPteMinScore(dto.getPteMinScore());
                    newCourse.setPteMinBandScore(dto.getPteMinBandScore());
                    newCourse.setDetMinScore(dto.getDetMinScore());
                    newCourse.setGreMinScore(dto.getGreMinScore());
                    newCourse.setGmatMinScore(dto.getGmatMinScore());
                    newCourse.setSatMinScore(dto.getSatMinScore());
                    newCourse.setCatMinScore(dto.getCatMinScore());
                    newCourse.setMin10thScore(dto.getMin10thScore());
                    newCourse.setMinInterScore(dto.getMinInterScore());
                    newCourse.setMinGraduationScore(dto.getMinGraduationScore());
                    newCourse.setScholarshipEligible(dto.getScholarshipEligible());
                    newCourse.setScholarshipDetails(dto.getScholarshipDetails());
                    newCourse.setBacklogAcceptanceRange(dto.getBacklogAcceptanceRange());
                    newCourse.setRemarks(dto.getRemarks());
                    newCourse.setCreatedAt(LocalDateTime.now());
                    newCourse.setUpdatedAt(LocalDateTime.now());

                    collegeCourses.add(newCourse);
                }
            }

            // Step 4: Perform batch insert
            batchInsert(collegeCourses);

            return "College Courses Uploaded Successfully!";
        } catch (Exception e) {
            throw new RuntimeException("Bulk Insert failed!", e);
        }
    }

    @Transactional
    public void batchInsert(List<CollegeCourse> courses) {
        int batchSize = 500;
        for (int i = 0; i < courses.size(); i++) {
            entityManager.persist(courses.get(i));

            if (i % batchSize == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public SearchCourseResponseDto<CollegeCourseResponseDto> getCollegeCourses(SearchCourseRequestDto searchCourseRequestDto) {
//        Mono<CurrencyResponseDTO> currencyResponseDTO = currencyAPIService.fetchData().map(response ->  response);
//        System.out.println(currencyResponseDTO);
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

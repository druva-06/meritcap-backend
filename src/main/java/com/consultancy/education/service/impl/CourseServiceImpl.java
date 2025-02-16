package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeRequestDto;
import com.consultancy.education.DTOs.requestDTOs.course.CourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.course.CourseResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.DatabaseException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.helper.ExcelHelper;
import com.consultancy.education.model.Course;
import com.consultancy.education.repository.CourseRepository;
import com.consultancy.education.service.CourseService;
import com.consultancy.education.transformer.CourseTransformer;
import com.consultancy.education.utils.PatternConvert;
import com.consultancy.education.validations.CourseValidations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CourseServiceImpl implements CourseService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseResponseDto addCourse(CourseRequestDto courseRequestDto) {
        if(courseRepository.findDuplicate(
                courseRequestDto.getName(),
                courseRequestDto.getDepartment(),
                courseRequestDto.getGraduationLevel(),
                courseRequestDto.getSpecialization())==null){
            Course course = CourseTransformer.toEntity(courseRequestDto);
            course = courseRepository.save(course);
            return CourseTransformer.toResDTO(course);
        }
        else{
            List<String> errors = new ArrayList<>();
            errors.add(courseRequestDto.getName() + " already exists");
            throw new AlreadyExistException(errors);
        }
    }

    @Override
    public List<CourseResponseDto> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return CourseTransformer.toResDTO(courses);
    }

    @Override
    public Long getACourseCount() {
        return courseRepository.count();
    }

    @Override
    public CourseResponseDto updateCourse(CourseRequestDto courseRequestDto, Long courseId) {
        if(courseRepository.findById(courseId).isPresent()){
            Course course = courseRepository.findById(courseId).get();
            Course duplicateCourse = courseRepository.findDuplicate(
                    courseRequestDto.getName(),
                    courseRequestDto.getDepartment(),
                    courseRequestDto.getGraduationLevel(),
                    courseRequestDto.getSpecialization()
            );
            if(duplicateCourse==null || Objects.equals(duplicateCourse.getId(), course.getId())){
                CourseTransformer.updateCourse(course, courseRequestDto);
                course = courseRepository.save(course);
                return CourseTransformer.toResDTO(course);
            }
            List<String> errors = new ArrayList<>();
            errors.add(courseRequestDto.getName() + " already exists");
            throw new AlreadyExistException(errors);
        }
        else{
            throw new NotFoundException("Course not found");
        }
    }

    @Override
    public CourseResponseDto deleteCourse(Long courseId) {
        if(courseRepository.findById(courseId).isPresent()){
            Course course = courseRepository.findById(courseId).get();
            courseRepository.delete(course);
            return CourseTransformer.toResDTO(course);
        }
        throw new NotFoundException("Course not found");
    }

    @Override
    public List<CourseResponseDto> getCourseByName(String name) {
        List<Course> courses = courseRepository.searchByNameOrDepartment(PatternConvert.jumbleSearch(name), PageRequest.of(0, 5));
        if (!courses.isEmpty()) {
            return CourseTransformer.toResDTO(courses);
        }
        else{
            throw new NotFoundException("College not found");
        }
    }

    @Override
    @Transactional
    public String bulkCoursesUpload(MultipartFile file) {
        try {
            List<CourseRequestDto> courses = ExcelHelper.convertCourseExcelIntoList(file.getInputStream());

            if (courses.isEmpty()) {
                return "No courses to upload";
            }

            // SQL Query Template
            String sql = """
                INSERT INTO courses (
                    name, department, graduation_level, specialization, created_at, updated_at
                ) VALUES %s
                ON DUPLICATE KEY UPDATE 
                    specialization = VALUES(specialization), 
                    updated_at = NOW()
            """;

            StringBuilder values = new StringBuilder();
            int batchSize = 1000;
            int count = 0;
            List<String> batchQueries = new ArrayList<>();

            for (CourseRequestDto course : courses) {
                values.append(String.format(
                        "('%s', '%s', '%s', %s, NOW(), NOW()),",
                        escapeSqlString(course.getName()),
                        escapeSqlString(course.getDepartment()),
                        escapeSqlString(course.getGraduationLevel().name()), // ENUM stored as String
                        (course.getSpecialization() != null ? "'" + escapeSqlString(course.getSpecialization()) + "'" : "NULL")
                ));

                count++;
                if (count % batchSize == 0) {
                    batchQueries.add(String.format(sql, values.substring(0, values.length() - 1)));
                    values.setLength(0);
                }
            }

            // Add remaining batch
            if (!values.isEmpty()) {
                batchQueries.add(String.format(sql, values.substring(0, values.length() - 1)));
            }

            // Execute batch queries
            for (String batchQuery : batchQueries) {
                entityManager.createNativeQuery(batchQuery).executeUpdate();
            }

            return "Courses Uploaded Successfully!";

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Bulk Insert/Update failed!", e);
        }
    }

    private String escapeSqlString(String input) {
        return input.replace("'", "''");
    }
}

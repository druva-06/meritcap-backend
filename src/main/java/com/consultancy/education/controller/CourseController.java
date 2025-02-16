package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.course.CourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.course.CourseResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.helper.ExcelHelper;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.CourseService;
import com.consultancy.education.utils.ToMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCourse(@RequestBody @Valid CourseRequestDto courseRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try{
            CourseResponseDto courseResponseDto =  courseService.addCourse(courseRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(courseResponseDto, "Course created successfully", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/bulkCourseUpload")
    public ResponseEntity<?> bulkCourseUpload(@RequestParam("file") MultipartFile file){
        if(ExcelHelper.checkExcelFormat(file)) {
            try{
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(courseService.bulkCoursesUpload(file), "Success!", 201));
            }
            catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(),"Incorrect excel format", 400));
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllCourses() {
        try {
            List<CourseResponseDto> courseResponseDtos = courseService.getAllCourses();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(courseResponseDtos, "Courses fetched successfully", 200));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/getCount")
    public ResponseEntity<?> getCourseCount() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(courseService.getACourseCount(), "Total courses fetched successfully", 200));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update/{courseId}")
    public ResponseEntity<?> updateCourse(@RequestBody @Valid CourseRequestDto courseRequestDto, BindingResult bindingResult, @PathVariable Long courseId) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(courseService.updateCourse(courseRequestDto, courseId), "Course updated successfully", 200));
        }
        catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(courseService.deleteCourse(courseId), "Course deleted successfully", 200));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/getByName")
    public ResponseEntity<?> getCourseByName(@RequestParam String name) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(courseService.getCourseByName(name), "Course fetched successfully", 200));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

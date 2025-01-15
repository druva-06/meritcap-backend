package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.search.SearchCourseRequestDto;
import com.consultancy.education.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.consultancy.education.DTOs.responseDTOs.search.SearchCourseResponseDto;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.CollegeCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@RestController
@RequestMapping("/college-course")
public class CollegeCourseController {

    private final CollegeCourseService collegeCourseService;

    public CollegeCourseController(CollegeCourseService collegeCourseService) {
        this.collegeCourseService = collegeCourseService;
    }

    @PostMapping("/bulkCollegeCourseUpload")
    public ResponseEntity<?> bulkCollegeCourseUpload(@RequestParam("file") MultipartFile file) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(collegeCourseService.bulkCollegeCourseUpload(file), "College courses added successfully", 201));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/collegeCourses")
    public ResponseEntity<?> getCollegeCourses(@RequestBody SearchCourseRequestDto searchCourseRequestDto) {
        try{
            SearchCourseResponseDto<CollegeCourseResponseDto> searchCourseResponseDto = collegeCourseService.getCollegeCourses(searchCourseRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(searchCourseResponseDto, "College courses fetched successfully", 200));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

//    @PutMapping("/update/{collegeCourseId}")
//    public ResponseEntity<?> updateCollegeCourse(@RequestBody @Valid CollegeCourseRequestExcelDto collegeCourseRequestExcelDto, BindingResult bindingResult, @PathVariable Long collegeCourseId){
//        if (bindingResult.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
//        }
//        try{
//            CollegeCourseResponseDto collegeCourseResponseDto = collegeCourseService.updateCollegeCourse(collegeCourseRequestExcelDto, collegeCourseId);
//            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(collegeCourseResponseDto, "College course updated successfully", 200));
//        }
//        catch (NotFoundException e){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
//        }
//        catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
//        }
//    }
//
//    @DeleteMapping("/delete/{collegeCourseId}")
//    public ResponseEntity<?> deleteCollegeCourse(@PathVariable Long collegeCourseId){
//        try{
//            CollegeCourseResponseDto collegeCourseResponseDto = collegeCourseService.deleteCollegeCourse(collegeCourseId);
//            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(collegeCourseResponseDto, "College course deleted successfully", 200));
//        }
//        catch (NotFoundException e){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
//        }
//        catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
//        }
//    }

}

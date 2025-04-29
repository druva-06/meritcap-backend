package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.StudentEducationService;
import com.consultancy.education.utils.ToMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/student-education")
public class StudentEducationController {

    private final StudentEducationService studentEducationService;

    public StudentEducationController(StudentEducationService studentEducationService) {
        this.studentEducationService = studentEducationService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStudentEducation(@RequestBody @Valid StudentEducationRequestDto studentEducationRequestDto, BindingResult bindingResult, @RequestParam Long userId) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation Failed", 400));
        }
        try{
            StudentEducationResponseDto studentEducationResponseDto = studentEducationService.addStudentEducation(studentEducationRequestDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(studentEducationResponseDto, "Student education added successfully", 201));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStudentEducation(@RequestBody @Valid StudentEducationRequestDto studentEducationRequestDto, @RequestParam Long studentEducationId, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation Failed", 400));
        }
        try{
            StudentEducationResponseDto studentEducationResponseDto = studentEducationService.updateStudentEducation(studentEducationRequestDto, studentEducationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(studentEducationResponseDto, "Student education updated successfully", 201));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getStudentEducationById(@RequestParam Long userId) {
        try{
            List<StudentEducationResponseDto> studentEducationResponseDtos = studentEducationService.getStudentEducation(userId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(studentEducationResponseDtos, "Student education fetched successfully", 200));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.student.StudentRequestDto;
import com.consultancy.education.DTOs.requestDTOs.student.StudentUpdateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.student.StudentResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.ValidationException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.StudentService;
import com.consultancy.education.utils.ToMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    @PostMapping("/add")
    public ResponseEntity<?> addStudent(@RequestBody @Valid StudentRequestDto studentRequestDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation Failed", 400));
        }
        try{
            StudentResponseDto studentResponseDto = studentService.addStudent(studentRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(studentResponseDto, "Student details updated successfully", 200));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<?> getStudent(@PathVariable Long userId) {
        try{
            StudentResponseDto studentResponseDto = studentService.getStudent(userId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(studentResponseDto, "Student details fetched successfully", 200));
        }
        catch (CustomException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiSuccessResponse<>(new ApiFailureResponse<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStudent(@RequestBody @Valid StudentUpdateRequestDto studentUpdateRequestDto, BindingResult bindingResult, @RequestParam Long studentId) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation Failed", 400));
        }
        try{
            StudentResponseDto studentResponseDto = studentService.updateStudent(studentUpdateRequestDto, studentId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(studentResponseDto, "Student updated successfully", 200));
        }
        catch (ValidationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 400));
        }
        catch (AlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

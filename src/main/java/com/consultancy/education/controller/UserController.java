package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.user.UserRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.exception.ValidationException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.UserService;
import com.consultancy.education.utils.ToMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png", "image/jpg");


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),"Validation failed", 400));
        }
        try{
            UserResponseDto responseDto = userService.addUser(userRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(responseDto, "User created successfully", 201));
        }
        catch (AlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult, Long userId) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),"Validation failed", 400));
        }
        try{
            UserResponseDto responseDto = userService.updateUser(userRequestDto, userId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(responseDto, "User updated successfully", 200));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (AlreadyExistException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getUser(@RequestParam Long userId) {
        try{
            UserResponseDto responseDto = userService.getUser(userId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(responseDto, "User fetched successfully", 200));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/uploadFile/{userId}/{documentType}")
    public ResponseEntity<?> uploadFile(@PathVariable Long userId, @PathVariable String documentType,
                             @RequestParam("file") MultipartFile file) {
        try{
            // Get the content type (MIME type) of the file
            String contentType = file.getContentType();

            // Check if the file's content type is in the allowed list
            if(!ALLOWED_FILE_TYPES.contains(contentType)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(), "Invalid file format", 400));
            }

            String response = userService.uploadFile(userId, documentType, file);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(new ArrayList<>(), response, 200));
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

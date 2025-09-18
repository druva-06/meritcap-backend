package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.consultancy.education.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.UserAuthService;
import com.consultancy.education.utils.ToMap;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Student Auth Controller", description = "Handles all student authentication related operations")
public class UserAuthController {

    private final UserAuthService userAuthService;

    UserAuthController(UserAuthService userAuthService){
        this.userAuthService = userAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserAuthSignUpRequestDto userAuthSignUpRequestDto, BindingResult bindingResult) {
        log.info("Signup request received: {}", userAuthSignUpRequestDto);
        if(bindingResult.hasErrors()) {
            log.error("Signup Validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),"Validation failed", 400));
        }
        try {
            String response = userAuthService.signup(userAuthSignUpRequestDto);
            log.info("Signup response: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(response, "Registered Successfully!", 201));
        }
        catch (CustomException e){
            log.error("Cognito Signup Validation errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Signup Validation errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserAuthLoginRequestDto userAuthLoginRequestDto, BindingResult bindingResult) {
        log.info("Login request received: {}", userAuthLoginRequestDto);
        if(bindingResult.hasErrors()) {
            log.error("Login Validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult),"Validation failed", 400));
        }
        try {
            UserAuthLoginResponseDto response = userAuthService.login(userAuthLoginRequestDto);
            log.info("Login response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Logged Successfully!", 200));
        }
        catch (CustomException e){
            log.error("Cognito Login Validation errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Login Validation errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/resendVerificationCode/{email}")
    public ResponseEntity<?> resendVerificationCode(@PathVariable String email){
        log.info("Resend verification code request received: {}", email);
        try {
            String response = userAuthService.resendVerificationCode(email);
            log.info("Resend verification code response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Resend verification code successfully!", 200));
        }
        catch (NotFoundException e){
            log.error("Cognito Resend Email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (CustomException e){
            log.error("Cognito Resend errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Resend errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/confirmVerificationCode")
    public ResponseEntity<?> confirmVerificationCode(@RequestParam String email, @RequestParam String verificationCode){
        log.info("Confirm verification code request received: {}", email);
        try {
            String response = userAuthService.confirmVerificationCode(email, verificationCode);
            log.info("Confirm verification code response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Verified Successfully!", 200));
        }
        catch (NotFoundException e){
            log.error("Cognito Confirm Email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (CustomException e){
            log.error("Cognito Confirm errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Confirm errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/forgotPassword/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable String email){
        log.info("Forgot password request received: {}", email);
        try {
            String response = userAuthService.forgotPassword(email);
            log.info("Forgot password response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Forgot password verification code sent successfully!", 200));
        }
        catch (NotFoundException e){
            log.error("Cognito forgot password email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (CustomException e){
            log.error("Cognito forgot password errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Forgot password errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/confirmForgotPassword")
    public ResponseEntity<?> confirmForgotPassword(@RequestParam String email, @RequestParam String confirmationCode, @RequestParam String newPassword){
       log.info("Confirm password request received: {}", email);
        try {
            String response = userAuthService.confirmForgotPassword(email, confirmationCode, newPassword);
            log.info("Confirm password response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Password changed Successfully!", 200));
        }
        catch (NotFoundException e){
            log.error("Cognito confirm forgot password email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        }
        catch (CustomException e){
           log.error("Cognito confirm forgot password errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        }
        catch (Exception e){
            log.error("Confirm password errors occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto, BindingResult bindingResult, @RequestHeader("Authorization") String authHeader) {
        log.info("Change password request received.");
        if (bindingResult.hasErrors()) {
            log.error("Change password validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            String jwtToken = authHeader.replace("Bearer ", "");
            String response = userAuthService.changePassword(jwtToken, changePasswordRequestDto);
            log.info("Change password response: {}", response);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(response, "Password changed successfully!", 200));
        } catch (CustomException e) {
            log.error("Change password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Change password internal error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

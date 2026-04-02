package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.SendEmailOTPRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthRefreshRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.VerifyEmailOTPRequestDto;
import com.meritcap.DTOs.responseDTOs.userAuth.SendEmailOTPResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthRefreshResponseDto;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.UserAuthService;
import com.meritcap.utils.ToMap;
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid UserAuthRefreshRequestDto refreshTokenRequestDto, BindingResult bindingResult) {
        log.info("Refresh token request received: {}", refreshTokenRequestDto.getRefreshToken());
        if (bindingResult.hasErrors()) {
            log.error("Refresh token validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }

        try {
            UserAuthRefreshResponseDto response = userAuthService.refreshAccessToken(refreshTokenRequestDto);
            log.info("Refresh token response successful for email: {}", refreshTokenRequestDto.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Token refreshed Successfully!", 200));
        } catch (CustomException e) {
            log.error("Cognito refresh token error occurred: {}", e.getMessage());
            // keep consistent with your other endpoints (they often return 200 with ApiFailureResponse for Cognito problems)
            return ResponseEntity.status(HttpStatus.OK).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Refresh token internal error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/email-otp/send")
    public ResponseEntity<?> sendEmailOTP(@RequestBody @Valid SendEmailOTPRequestDto request, BindingResult bindingResult) {
        log.info("Send OTP request received for email: {}", request.getEmail());
        if (bindingResult.hasErrors()) {
            log.error("Send OTP validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }

        try {
            SendEmailOTPResponseDto response = userAuthService.sendEmailOTP(request);
            log.info("OTP sent successfully to: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "OTP sent successfully!", 200));
        } catch (CustomException e) {
            log.error("Send OTP error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Send OTP internal error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/email-otp/verify")
    public ResponseEntity<?> verifyEmailOTP(@RequestBody @Valid VerifyEmailOTPRequestDto request, BindingResult bindingResult) {
        log.info("Verify OTP request received for email: {}", request.getEmail());
        if (bindingResult.hasErrors()) {
            log.error("Verify OTP validation errors occurred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }

        try {
            UserAuthLoginResponseDto response = userAuthService.verifyEmailOTP(request);
            log.info("OTP verified successfully for: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponse<>(response, "Login successful!", 200));
        } catch (CustomException e) {
            log.error("Verify OTP error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Verify OTP internal error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}

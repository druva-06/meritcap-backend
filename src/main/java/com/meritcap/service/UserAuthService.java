package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.SendEmailOTPRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthRefreshRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.VerifyEmailOTPRequestDto;
import com.meritcap.DTOs.responseDTOs.userAuth.SendEmailOTPResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthRefreshResponseDto;
import jakarta.validation.Valid;

public interface UserAuthService {
    String signup(@Valid UserAuthSignUpRequestDto userAuthSignUpRequestDto);

    UserAuthLoginResponseDto login(@Valid UserAuthLoginRequestDto userAuthLoginRequestDto);

    String resendVerificationCode(String email);

    String confirmVerificationCode(String email, String verificationCode);

    String forgotPassword(String email);

    String confirmForgotPassword(String email, String confirmationCode, String newPassword);

    String changePassword(String jwtToken, @Valid ChangePasswordRequestDto changePasswordRequestDto);

    UserAuthRefreshResponseDto refreshAccessToken(UserAuthRefreshRequestDto refreshTokenRequestDto);

    SendEmailOTPResponseDto sendEmailOTP(@Valid SendEmailOTPRequestDto request);

    UserAuthLoginResponseDto verifyEmailOTP(@Valid VerifyEmailOTPRequestDto request);

}

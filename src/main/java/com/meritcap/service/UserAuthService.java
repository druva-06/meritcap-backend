package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.GoogleAuthCallbackRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.SendEmailOTPRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UpdateUsernameRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthRefreshRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.VerifyEmailOTPRequestDto;
import com.meritcap.DTOs.responseDTOs.userAuth.GoogleAuthUrlResponseDto;
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

    /**
     * Get the Google OAuth URL for redirecting users to Google sign-in
     * @param redirectUri The URI to redirect to after Google authentication
     * @return GoogleAuthUrlResponseDto containing the auth URL and state
     */
    GoogleAuthUrlResponseDto getGoogleAuthUrl(String redirectUri);

    /**
     * Handle the Google OAuth callback - exchange code for tokens and create/login user
     * @param request The callback request containing authorization code
     * @return UserAuthLoginResponseDto with user info and tokens
     */
    UserAuthLoginResponseDto handleGoogleCallback(@Valid GoogleAuthCallbackRequestDto request);

    /**
     * Ensures a Cognito user is present in the STUDENT group. Safe to call repeatedly.
     * @param email user email
     */
    void ensureStudentGroupMembership(String email);

    /**
     * Update the username for users with incomplete profiles (OAuth users)
     * @param userId The user ID
     * @param request The request containing the new username
     * @return Success message
     */
    String updateUsername(Long userId, @Valid UpdateUsernameRequestDto request);

    /**
     * Update the phone number for users with placeholder phones
     * @param userId The user ID
     * @param phoneNumber The new phone number
     * @return Success message
     */
    String updatePhoneNumber(Long userId, String phoneNumber);

}

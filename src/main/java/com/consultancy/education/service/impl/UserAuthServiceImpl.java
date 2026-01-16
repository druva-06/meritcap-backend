package com.consultancy.education.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.consultancy.education.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthRefreshRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.consultancy.education.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.consultancy.education.DTOs.responseDTOs.userAuth.UserAuthRefreshResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.model.Role;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.RoleRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.service.UserAuthService;
import com.consultancy.education.transformer.UserAuthTransformer;
import com.consultancy.education.transformer.UserTransformer;
import com.consultancy.education.utils.CognitoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    UserAuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            CognitoIdentityProviderClient cognitoClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cognitoClient = cognitoClient;
    }

    @Override
    public String signup(UserAuthSignUpRequestDto userAuthSignUpRequestDto) {
        log.info("Signup service started for email: {}", userAuthSignUpRequestDto.getEmail());

        if (userRepository.findByEmail(userAuthSignUpRequestDto.getEmail()) != null) {
            log.error("Email Already Exists: {}", userAuthSignUpRequestDto.getEmail());
            throw new CustomException("Email Already Exists");
        }

        if (userRepository.findByPhoneNumber(userAuthSignUpRequestDto.getPhoneNumber()) != null) {
            log.error("Phone Already Exists: {}", userAuthSignUpRequestDto.getPhoneNumber());
            throw new CustomException("Phone Number Already Exists");
        }

        if (userRepository.findByUsername(userAuthSignUpRequestDto.getUsername()) != null) {
            log.error("Username Already Exists: {}", userAuthSignUpRequestDto.getUsername());
            throw new CustomException("Username Already Exists");
        }

        Map<String, AttributeType> attributes = new HashMap<>();
        attributes.put("email",
                AttributeType.builder().name("email").value(userAuthSignUpRequestDto.getEmail()).build());
        attributes.put("phone_number",
                AttributeType.builder().name("phone_number").value(userAuthSignUpRequestDto.getPhoneNumber()).build());
        attributes.put("name",
                AttributeType.builder().name("name").value(userAuthSignUpRequestDto.getFirstName()).build());
        attributes.put("preferred_username", AttributeType.builder().name("preferred_username")
                .value(userAuthSignUpRequestDto.getUsername()).build());

        String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret,
                userAuthSignUpRequestDto.getEmail());

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .username(userAuthSignUpRequestDto.getEmail())
                .password(userAuthSignUpRequestDto.getPassword())
                .userAttributes(attributes.values())
                .secretHash(secretHash)
                .build();

        log.debug("SignUpRequest prepared: {}", signUpRequest);

        try {
            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            log.info("User signup request sent to Cognito for email: {}", userAuthSignUpRequestDto.getEmail());

            // Add user to Cognito group
            String groupName = userAuthSignUpRequestDto.getRole(); // role is already a String
            AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userAuthSignUpRequestDto.getEmail())
                    .groupName(groupName)
                    .build();
            cognitoClient.adminAddUserToGroup(groupRequest);
            log.info("User {} added to Cognito group: {}", userAuthSignUpRequestDto.getEmail(), groupName);

            // Save user in local database
            // Fetch role from database
            String roleName = userAuthSignUpRequestDto.getRole();
            Role role = roleRepository.findByNameIgnoreCase(roleName)
                    .orElseThrow(() -> new CustomException("Role not found: " + roleName));

            User user = UserAuthTransformer.toUserEntity(userAuthSignUpRequestDto, role);

            // Only create Student entity if the role is STUDENT
            if ("STUDENT".equalsIgnoreCase(userAuthSignUpRequestDto.getRole())) {
                Student student = new Student();
                student.setUser(user);
                user.setStudent(student);
                log.info("Student record created for user: {}", userAuthSignUpRequestDto.getEmail());
            }

            userRepository.save(user);

            log.info("User {} saved in local database", userAuthSignUpRequestDto.getEmail());
            return "User Registered Successfully!";
        } catch (CognitoIdentityProviderException e) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.error("Signup error for email {}: {}", userAuthSignUpRequestDto.getEmail(), errorMessage);
            if (errorMessage.contains("An account with the given email already exists")) {
                throw new CustomException("User already exists. Please log in.");
            } else if (errorMessage.contains("Invalid email address format")) {
                throw new CustomException("Invalid email format.");
            } else if (errorMessage.contains("Invalid phone number format")) {
                throw new CustomException("Please enter a valid phone number.");
            } else if (errorMessage.contains("Weak password")) {
                throw new CustomException("Password is too weak. Use a stronger password.");
            } else {
                throw new CustomException(errorMessage);
            }
        } catch (Exception ex) {
            log.error("Unexpected error during signup for email {}: {}", userAuthSignUpRequestDto.getEmail(),
                    ex.getMessage(), ex);
            throw new CustomException("Unexpected error during signup. Please try again.");
        }
    }

    @Override
    public UserAuthLoginResponseDto login(UserAuthLoginRequestDto userAuthLoginRequestDto) {
        log.info("Login service started for email: {}", userAuthLoginRequestDto.getEmail());

        User user = userRepository.findByEmail(userAuthLoginRequestDto.getEmail());
        if (user != null && Boolean.TRUE.equals(user.getAccountLocked())) {
            log.warn("Account is locked for user: {}", user.getEmail());
            throw new CustomException(
                    "Account is locked due to too many failed login attempts. Please reset your password or contact support.");
        }

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userAuthLoginRequestDto.getEmail());
        authParams.put("PASSWORD", userAuthLoginRequestDto.getPassword());
        authParams.put("SECRET_HASH",
                CognitoUtil.calculateSecretHash(clientId, clientSecret, userAuthLoginRequestDto.getEmail()));

        log.debug("AuthParams: {}", authParams.keySet());

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();

        try {
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            log.info("Cognito authentication successful for email: {}", userAuthLoginRequestDto.getEmail());

            // Reset failed attempts
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setAccountLocked(false);
                userRepository.save(user);
                log.debug("Reset failed login attempts for user: {}", user.getEmail());
            }

            UserAuthLoginResponseDto userAuthLoginResponseDto = UserAuthTransformer.toLoginResDto(authResponse);

            DecodedJWT jwt = JWT.decode(userAuthLoginResponseDto.getIdToken());
            String email = jwt.getClaim("email").asString();

            if (email == null) {
                log.error("JWT token does not contain email claim for user: {}", userAuthLoginRequestDto.getEmail());
                throw new CustomException("Invalid JWT Token.");
            }

            // Find user and map details
            user = userRepository.findByEmail(email);
            if (user == null) {
                log.error("User not found in database for email: {}", email);
                throw new CustomException("User not found.");
            }

            UserTransformer.intoUserAuthLoginRes(user, userAuthLoginResponseDto);

            log.info("Login process completed successfully for email: {}", email);

            return userAuthLoginResponseDto;
        } catch (CognitoIdentityProviderException e) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.warn("Login failed for email: {}. Reason: {}", userAuthLoginRequestDto.getEmail(), errorMessage);

            // Increase failed attempts for this user
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
                attempts++;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLocked(true);
                    log.warn("User account locked due to too many failed attempts: {}", user.getEmail());
                }
                userRepository.save(user);
            }

            if (errorMessage.contains("User is not confirmed")) {
                throw new CustomException("User account is not confirmed. Please verify your email.");
            } else if (errorMessage.contains("Incorrect username or password")) {
                throw new CustomException("Incorrect email or password.");
            } else if (errorMessage.contains("User does not exist")) {
                throw new CustomException("No account found with this email.");
            } else {
                throw new CustomException(errorMessage);
            }
        } catch (Exception ex) {
            log.error("Unexpected error during login for email: {}. Details: {}", userAuthLoginRequestDto.getEmail(),
                    ex.getMessage());
            throw new CustomException("Internal server error.");
        }
    }

    @Override
    public String confirmVerificationCode(String email, String verificationCode) {
        log.info("Attempting to confirm verification code for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Email for verification is null or empty.");
            throw new CustomException("Email cannot be empty.");
        }
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            log.warn("Verification code is null or empty for email: {}", email);
            throw new CustomException("Verification code cannot be empty.");
        }

        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(verificationCode)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.confirmSignUp(confirmRequest);

            log.info("Account successfully verified for email: {}", email);
            return "Account successfully verified.";
        } catch (CognitoIdentityProviderException e) {
            String error = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Error confirming verification code for email: {}. Reason: {}", email, error);
            if (error != null && error.contains("expired")) {
                throw new CustomException("The verification code has expired. Please request a new code.");
            } else if (error != null && error.contains("Invalid")) {
                throw new CustomException("Invalid verification code.");
            } else if (error != null && error.contains("already confirmed")) {
                throw new CustomException("Account is already verified. Please log in.");
            }
            throw new CustomException("Failed to verify account. " + (error != null ? error : ""));
        } catch (Exception e) {
            log.error("Unexpected error while confirming verification for email: {}", email, e);
            throw new CustomException("An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    public String resendVerificationCode(String email) {
        log.info("Resend verification code attempt for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Resend verification: Email is null or empty.");
            throw new CustomException("Email cannot be empty.");
        }

        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ResendConfirmationCodeRequest resendRequest = ResendConfirmationCodeRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.resendConfirmationCode(resendRequest);

            log.info("Verification code resent successfully to email: {}", email);
            return "Verification code has been resent to your email.";
        } catch (CognitoIdentityProviderException e) {
            String error = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Error resending verification code for email: {}. Reason: {}", email, error);
            if (error != null && error.contains("already confirmed")) {
                throw new CustomException("Account is already verified. Please log in.");
            } else if (error != null && error.contains("not found")) {
                throw new CustomException("No account found with this email.");
            }
            throw new CustomException("Failed to resend verification code. " + (error != null ? error : ""));
        } catch (Exception e) {
            log.error("Unexpected error while resending verification code for email: {}", email, e);
            throw new CustomException("An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    public String forgotPassword(String email) {
        log.info("Forgot password request for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Forgot password: Email is null or empty.");
            throw new CustomException("Email cannot be empty.");
        }

        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ForgotPasswordRequest forgotRequest = ForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.forgotPassword(forgotRequest);

            log.info("Password reset code sent successfully to email: {}", email);
            return "Password reset code has been sent to your email.";
        } catch (CognitoIdentityProviderException e) {
            String error = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Error sending password reset code for email: {}. Reason: {}", email, error);
            if (error != null && error.contains("not found")) {
                throw new CustomException("No account found with this email.");
            } else if (error != null && error.contains("User is not confirmed")) {
                throw new CustomException("User account is not confirmed. Please verify your email.");
            }
            throw new CustomException("Failed to send password reset code. " + (error != null ? error : ""));
        } catch (Exception e) {
            log.error("Unexpected error during forgot password for email: {}", email, e);
            throw new CustomException("An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    public String confirmForgotPassword(String email, String confirmationCode, String newPassword) {
        log.info("Confirm forgot password for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Confirm forgot password: Email is null or empty.");
            throw new CustomException("Email cannot be empty.");
        }
        if (confirmationCode == null || confirmationCode.trim().isEmpty()) {
            log.warn("Confirm forgot password: Confirmation code is null or empty for email: {}", email);
            throw new CustomException("Confirmation code cannot be empty.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            log.warn("Confirm forgot password: New password is null or empty for email: {}", email);
            throw new CustomException("New password cannot be empty.");
        }

        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ConfirmForgotPasswordRequest confirmRequest = ConfirmForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .password(newPassword)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.confirmForgotPassword(confirmRequest);

            log.info("Password has been reset successfully for email: {}", email);
            return "Password has been successfully reset.";
        } catch (CognitoIdentityProviderException e) {
            String error = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Error confirming password reset for email: {}. Reason: {}", email, error);
            if (error != null && error.contains("expired")) {
                throw new CustomException("The confirmation code has expired. Please request a new code.");
            } else if (error != null && error.contains("Invalid")) {
                throw new CustomException("Invalid confirmation code.");
            }
            throw new CustomException("Failed to reset password. " + (error != null ? error : ""));
        } catch (Exception e) {
            log.error("Unexpected error during confirm forgot password for email: {}", email, e);
            throw new CustomException("An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    public String changePassword(String jwtToken, ChangePasswordRequestDto changePasswordRequestDto) {
        log.info("Change password service started.");
        try {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .accessToken(jwtToken)
                    .previousPassword(changePasswordRequestDto.getOldPassword())
                    .proposedPassword(changePasswordRequestDto.getNewPassword())
                    .build();
            cognitoClient.changePassword(request);
            log.info("Password changed successfully.");
            return "Password changed successfully.";
        } catch (CognitoIdentityProviderException e) {
            log.error("Cognito error while changing password: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Unexpected error during password change: {}", e.getMessage());
            throw new CustomException("Unexpected error occurred while changing password.");
        }
    }

    @Override
    public UserAuthRefreshResponseDto refreshAccessToken(UserAuthRefreshRequestDto refreshTokenRequestDto) {
        log.info("Refresh token service started for email: {}", refreshTokenRequestDto.getEmail());

        if (refreshTokenRequestDto.getEmail() == null || refreshTokenRequestDto.getEmail().trim().isEmpty()) {
            log.warn("Refresh token: email is null or empty");
            throw new CustomException("Email cannot be empty.");
        }
        if (refreshTokenRequestDto.getRefreshToken() == null
                || refreshTokenRequestDto.getRefreshToken().trim().isEmpty()) {
            log.warn("Refresh token: refreshToken is null or empty for email: {}", refreshTokenRequestDto.getEmail());
            throw new CustomException("Refresh token cannot be empty.");
        }

        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshTokenRequestDto.getRefreshToken());
            // secret hash is required in your codebase patterns (you use it for
            // login/signup)
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret,
                    refreshTokenRequestDto.getEmail());
            authParams.put("SECRET_HASH", secretHash);

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType result = authResponse.authenticationResult();

            if (result == null) {
                log.error("Cognito returned null authenticationResult for refresh token, email: {}",
                        refreshTokenRequestDto.getEmail());
                throw new CustomException("Unable to refresh token");
            }

            UserAuthRefreshResponseDto responseDto = new UserAuthRefreshResponseDto();
            responseDto.setAccessToken(result.accessToken());
            responseDto.setIdToken(result.idToken());
            responseDto.setTokenType(result.tokenType());
            responseDto.setExpiresIn(result.expiresIn());

            log.info("Refresh token successful for email: {}", refreshTokenRequestDto.getEmail());
            return responseDto;

        } catch (CognitoIdentityProviderException e) {
            String error = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Cognito refresh token error for email {}: {}", refreshTokenRequestDto.getEmail(), error);
            // Map common errors to friendly messages similar to your other methods
            if (error != null && error.contains("Invalid refresh token")) {
                throw new CustomException("Invalid or expired refresh token.");
            }
            throw new CustomException(error != null ? error : "Failed to refresh token.");
        } catch (Exception e) {
            log.error("Unexpected error while refreshing token for email {}: {}", refreshTokenRequestDto.getEmail(),
                    e.getMessage(), e);
            throw new CustomException("Unexpected error while refreshing token. Please try again later.");
        }
    }
}

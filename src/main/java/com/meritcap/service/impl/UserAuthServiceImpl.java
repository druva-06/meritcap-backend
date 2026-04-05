package com.meritcap.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.meritcap.DTOs.requestDTOs.userAuth.ChangePasswordRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.GoogleAuthCallbackRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.SendEmailOTPRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UpdateUsernameRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthRefreshRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.meritcap.DTOs.requestDTOs.userAuth.VerifyEmailOTPRequestDto;
import com.meritcap.DTOs.responseDTOs.permission.PermissionResponseDto;
import com.meritcap.DTOs.responseDTOs.user.UserPermissionsResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.GoogleAuthUrlResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.SendEmailOTPResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthRefreshResponseDto;
import com.meritcap.exception.CustomException;
import com.meritcap.model.EmailOTP;
import com.meritcap.model.Role;
import com.meritcap.model.Student;
import com.meritcap.model.User;
import com.meritcap.repository.EmailOTPRepository;
import com.meritcap.repository.RoleRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.service.EmailService;
import com.meritcap.service.PermissionService;
import com.meritcap.service.UserAuthService;
import com.meritcap.transformer.UserAuthTransformer;
import com.meritcap.transformer.UserTransformer;
import com.meritcap.utils.CognitoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailOTPRepository emailOTPRepository;
    private final EmailService emailService;
    private final CognitoIdentityProviderClient cognitoClient;
    private final PermissionService permissionService;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.domain:}")
    private String cognitoDomain;

    @Value("${aws.cognito.google.redirectUri:}")
    private String googleRedirectUri;

    @Value("${aws.cognito.region:ap-south-2}")
    private String cognitoRegion;

    UserAuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            EmailOTPRepository emailOTPRepository, EmailService emailService,
            CognitoIdentityProviderClient cognitoClient, PermissionService permissionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailOTPRepository = emailOTPRepository;
        this.emailService = emailService;
        this.cognitoClient = cognitoClient;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public String signup(UserAuthSignUpRequestDto userAuthSignUpRequestDto) {
        log.info("Signup service started for email: {}", userAuthSignUpRequestDto.getEmail());

        if (userRepository.findByEmail(userAuthSignUpRequestDto.getEmail()) != null) {
            log.error("Email Already Exists: {}", userAuthSignUpRequestDto.getEmail());
            throw new CustomException("Email Already Exists");
        }

        // Determine if phone is provided or needs placeholder
        String phoneNumber = userAuthSignUpRequestDto.getPhoneNumber();
        boolean hasRealPhone = phoneNumber != null && !phoneNumber.trim().isEmpty();
        
        if (hasRealPhone) {
            // Only check for duplicates if a real phone number is provided
            if (userRepository.findByPhoneNumber(phoneNumber) != null) {
                log.error("Phone Already Exists: {}", phoneNumber);
                throw new CustomException("Phone Number Already Exists");
            }
        } else {
            // Generate placeholder phone number (same pattern as OAuth signup)
            phoneNumber = "+1000000000" + (new java.util.Random().nextInt(9000) + 1000);
            log.info("Generated placeholder phone for user: {}", userAuthSignUpRequestDto.getEmail());
        }

        if (userRepository.findByUsername(userAuthSignUpRequestDto.getUsername()) != null) {
            log.error("Username Already Exists: {}", userAuthSignUpRequestDto.getUsername());
            throw new CustomException("Username Already Exists");
        }

        // Validate role exists in DB before calling Cognito
        String roleName = userAuthSignUpRequestDto.getRole();
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new CustomException("Role not found: " + roleName));

        Map<String, AttributeType> attributes = new HashMap<>();
        attributes.put("email",
                AttributeType.builder().name("email").value(userAuthSignUpRequestDto.getEmail()).build());
        attributes.put("phone_number",
                AttributeType.builder().name("phone_number").value(phoneNumber).build());
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

        // Track whether Cognito user was created so we can clean up on failure
        boolean cognitoUserCreated = false;
        String email = userAuthSignUpRequestDto.getEmail();

        try {
            // Step 1: Create user in Cognito
            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            cognitoUserCreated = true;
            log.info("User signup request sent to Cognito for email: {}", email);

            // Step 2: Add user to Cognito group
            String groupName = userAuthSignUpRequestDto.getRole();
            AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .groupName(groupName)
                    .build();
            cognitoClient.adminAddUserToGroup(groupRequest);
            log.info("User {} added to Cognito group: {}", email, groupName);

            // Step 3: Save user in local database (within @Transactional)
            User user = UserAuthTransformer.toUserEntity(userAuthSignUpRequestDto, role);
            
            // Override phone number with resolved value (may be placeholder)
            user.setPhoneNumber(phoneNumber);
            
            // Set profileIncomplete if user signed up without real phone
            if (!hasRealPhone) {
                user.setProfileIncomplete(true);
                log.info("User {} marked as profileIncomplete (no phone provided)", email);
            }

            if ("STUDENT".equalsIgnoreCase(userAuthSignUpRequestDto.getRole())) {
                Student student = new Student();
                student.setUser(user);
                user.setStudent(student);
                log.info("Student record created for user: {}", email);
            }

            userRepository.save(user);
            log.info("User {} saved in local database", email);

            return "User Registered Successfully!";

        } catch (CognitoIdentityProviderException e) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.error("Signup error for email {}: {}", email, errorMessage);

            // Cognito threw an error — clean up if user was partially created
            if (cognitoUserCreated) {
                deleteCognitoUserQuietly(email);
            }

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
            log.error("Unexpected error during signup for email {}: {}", email, ex.getMessage(), ex);

            // Clean up Cognito user if it was created but DB save failed
            if (cognitoUserCreated) {
                deleteCognitoUserQuietly(email);
            }

            throw new CustomException("Unexpected error during signup. Please try again.");
        }
    }

    /**
     * Attempts to delete a Cognito user silently for cleanup purposes.
     * Logs errors but does not throw — used during compensating rollback.
     */
    private void deleteCognitoUserQuietly(String email) {
        try {
            AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build();
            cognitoClient.adminDeleteUser(deleteRequest);
            log.info("Cleaned up Cognito user after signup failure: {}", email);
        } catch (Exception cleanupEx) {
            log.error("Failed to clean up Cognito user {} after signup failure: {}", email, cleanupEx.getMessage());
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

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .clientId(clientId)
                .userPoolId(userPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .build();

        try {
            AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
            log.info("Cognito authentication successful for email: {}", userAuthLoginRequestDto.getEmail());

            // Reset failed attempts
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setAccountLocked(false);
                userRepository.save(user);
                log.debug("Reset failed login attempts for user: {}", user.getEmail());
            }

            UserAuthLoginResponseDto userAuthLoginResponseDto = UserAuthTransformer.toAdminLoginResDto(authResponse);

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

            // Add user permissions to login response
            try {
                UserPermissionsResponseDto userPermissions = permissionService.getUserPermissions(user.getId());

                // Extract permission names
                List<String> allPermissionNames = userPermissions.getAllPermissions().stream()
                        .map(PermissionResponseDto::getName)
                        .collect(Collectors.toList());

                // Group permissions by category
                Map<String, List<String>> categorizedPermissions = userPermissions.getAllPermissions().stream()
                        .filter(p -> p.getCategory() != null)
                        .collect(Collectors.groupingBy(
                                PermissionResponseDto::getCategory,
                                Collectors.mapping(PermissionResponseDto::getName, Collectors.toList())));

                // Set permissions in response
                UserAuthLoginResponseDto.UserPermissionInfo permissionInfo = UserAuthLoginResponseDto.UserPermissionInfo
                        .builder()
                        .roleName(user.getRole().getName())
                        .allPermissions(allPermissionNames)
                        .categories(categorizedPermissions)
                        .build();

                userAuthLoginResponseDto.setPermissions(permissionInfo);

                log.info("Permissions added to login response for user: {}", email);
            } catch (Exception e) {
                log.error("Error fetching permissions for user {}: {}", email, e.getMessage());
                // Don't fail login if permission fetch fails, just log the error
            }

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

    @Override
    @Transactional
    public SendEmailOTPResponseDto sendEmailOTP(SendEmailOTPRequestDto request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Send OTP request for email: {}", email);

        // Check rate limiting: prevent sending OTP within 60 seconds
        Optional<EmailOTP> recentOTP = emailOTPRepository.findTopByEmailAndConsumedFalseOrderByCreatedAtDesc(email);
        if (recentOTP.isPresent()) {
            LocalDateTime lastSentAt = recentOTP.get().getCreatedAt();
            long secondsSinceLastSent = java.time.Duration.between(lastSentAt, LocalDateTime.now()).getSeconds();
            if (secondsSinceLastSent < 60) {
                long waitTime = 60 - secondsSinceLastSent;
                log.warn("Rate limit exceeded for email: {}. Wait {} seconds", email, waitTime);
                throw new CustomException(String.format("Please wait %d seconds before requesting a new code", waitTime));
            }
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // Save OTP to database
        EmailOTP emailOTP = EmailOTP.builder()
                .email(email)
                .otp(otp)
                .expiresAt(expiresAt)
                .consumed(false)
                .attempts(0)
                .build();
        emailOTPRepository.save(emailOTP);

        // Send OTP email
        try {
            emailService.sendOTPEmail(email, otp);
            log.info("OTP sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new CustomException("Failed to send OTP email. Please try again.");
        }

        return SendEmailOTPResponseDto.builder()
                .success(true)
                .message("OTP sent successfully to your email")
                .expiresIn(600) // 10 minutes
                .build();
    }

    @Override
    @Transactional
    public UserAuthLoginResponseDto verifyEmailOTP(VerifyEmailOTPRequestDto request) {
        String email = request.getEmail().toLowerCase().trim();
        String otp = request.getOtp();
        log.info("Verify OTP request for email: {}", email);

        // Find the most recent unconsumed OTP for this email
        Optional<EmailOTP> otpOptional = emailOTPRepository.findByEmailAndOtpAndConsumedFalse(email, otp);
        
        if (otpOptional.isEmpty()) {
            log.warn("Invalid OTP for email: {}", email);
            throw new CustomException("Invalid or expired OTP code");
        }

        EmailOTP emailOTP = otpOptional.get();

        // Check if OTP is expired
        if (emailOTP.isExpired()) {
            log.warn("Expired OTP for email: {}", email);
            throw new CustomException("OTP code has expired. Please request a new one");
        }

        // Check attempts limit
        if (emailOTP.getAttempts() >= 5) {
            log.warn("Max OTP attempts exceeded for email: {}", email);
            throw new CustomException("Maximum attempts exceeded. Please request a new code");
        }

        // Increment attempts
        emailOTP.setAttempts(emailOTP.getAttempts() + 1);

        // Validate OTP matches
        if (!emailOTP.getOtp().equals(otp)) {
            emailOTPRepository.save(emailOTP);
            log.warn("OTP mismatch for email: {}. Attempts: {}", email, emailOTP.getAttempts());
            throw new CustomException("Invalid OTP code");
        }

        // Mark OTP as consumed
        emailOTP.setConsumed(true);
        emailOTPRepository.save(emailOTP);

        // Check if user exists
        User existingUser = userRepository.findByEmail(email);
        
        if (existingUser != null) {
            // Existing user - generate tokens from Cognito
            log.info("Existing user login via OTP: {}", email);
            return generateTokensForExistingUser(existingUser);
        } else {
            // New user - create minimal account
            log.info("Creating new user via OTP: {}", email);
            return createMinimalUserAndGenerateTokens(email);
        }
    }

    private UserAuthLoginResponseDto generateTokensForExistingUser(User user) {
        try {
            log.info("Generating Cognito tokens for existing OTP user: {}", user.getEmail());
            
            // Generate real Cognito tokens using admin API
            // This requires the user to exist in Cognito with a password
            // We'll use the stored password or generate a new temporary one
            
            // Try to get or create Cognito tokens
            String tempPassword = UUID.randomUUID().toString() + "Aa1!";
            String email = user.getEmail();
            
            try {
                // First, ensure user exists in Cognito
                try {
                    AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .build();
                    cognitoClient.adminGetUser(getUserRequest);
                    log.info("OTP user exists in Cognito: {}", email);
                } catch (Exception e) {
                    // User doesn't exist in Cognito, create them
                    log.warn("OTP user not in Cognito, creating: {}", email);
                    
                    List<AttributeType> attributes = new ArrayList<>();
                    attributes.add(AttributeType.builder().name("email").value(email).build());
                    attributes.add(AttributeType.builder().name("email_verified").value("true").build());
                    attributes.add(AttributeType.builder().name("phone_number").value(user.getPhoneNumber()).build());
                    
                    AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .userAttributes(attributes)
                            .temporaryPassword(tempPassword)
                            .messageAction(MessageActionType.SUPPRESS)
                            .build();
                    cognitoClient.adminCreateUser(createUserRequest);
                    
                    // Set permanent password
                    AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .password(tempPassword)
                            .permanent(true)
                            .build();
                    cognitoClient.adminSetUserPassword(setPasswordRequest);
                    
                    // Add to student group
                    try {
                        AdminAddUserToGroupRequest addToGroupRequest = AdminAddUserToGroupRequest.builder()
                                .userPoolId(userPoolId)
                                .username(email)
                                .groupName("STUDENT")
                                .build();
                        cognitoClient.adminAddUserToGroup(addToGroupRequest);
                    } catch (Exception groupEx) {
                        log.warn("Could not add user to group: {}", groupEx.getMessage());
                    }
                }
                
                // Reset password to ensure we can authenticate
                AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .password(tempPassword)
                        .permanent(true)
                        .build();
                cognitoClient.adminSetUserPassword(setPasswordRequest);
                
                // Authenticate and get real tokens
                String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);
                
                Map<String, String> authParams = new HashMap<>();
                authParams.put("USERNAME", email);
                authParams.put("PASSWORD", tempPassword);
                authParams.put("SECRET_HASH", secretHash);
                
                AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                        .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                        .clientId(clientId)
                        .userPoolId(userPoolId)
                        .authParameters(authParams)
                        .build();
                
                AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
                
                // Create response with real Cognito tokens
                UserAuthLoginResponseDto response = UserAuthLoginResponseDto.builder()
                        .idToken(authResponse.authenticationResult().idToken())
                        .accessToken(authResponse.authenticationResult().accessToken())
                        .refreshToken(authResponse.authenticationResult().refreshToken())
                        .tokenType(authResponse.authenticationResult().tokenType())
                        .expiresIn(authResponse.authenticationResult().expiresIn())
                        .build();
                
                // Add user details
                UserTransformer.intoUserAuthLoginRes(user, response);
                
                // Add permissions
                try {
                    UserPermissionsResponseDto userPermissions = permissionService.getUserPermissions(user.getId());
                    
                    List<String> allPermissionNames = userPermissions.getAllPermissions().stream()
                            .map(PermissionResponseDto::getName)
                            .collect(Collectors.toList());
                    
                    Map<String, List<String>> categorizedPermissions = userPermissions.getAllPermissions().stream()
                            .filter(p -> p.getCategory() != null)
                            .collect(Collectors.groupingBy(
                                    PermissionResponseDto::getCategory,
                                    Collectors.mapping(PermissionResponseDto::getName, Collectors.toList())));
                    
                    UserAuthLoginResponseDto.UserPermissionInfo permissionInfo = UserAuthLoginResponseDto.UserPermissionInfo
                            .builder()
                            .roleName(user.getRole().getName())
                            .allPermissions(allPermissionNames)
                            .categories(categorizedPermissions)
                            .build();
                    
                    response.setPermissions(permissionInfo);
                } catch (Exception e) {
                    log.warn("Failed to load permissions for user {}: {}", user.getId(), e.getMessage());
                }
                
                log.info("Successfully generated real Cognito tokens for OTP user: {}", email);
                return response;
                
            } catch (Exception cognitoEx) {
                log.error("Failed to generate Cognito tokens for OTP user {}: {}", email, cognitoEx.getMessage(), cognitoEx);
                throw new CustomException("Failed to complete login. Please try again.");
            }
            
        } catch (Exception e) {
            log.error("Failed to generate tokens for user: {}", user.getEmail(), e);
            throw new CustomException("Failed to complete login. Please try again.");
        }
    }

    private UserAuthLoginResponseDto createMinimalUserAndGenerateTokens(String email) {
        try {
            // Get default student role
            Role studentRole = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new CustomException("Default student role not found"));

            // Generate unique username from email
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            while (userRepository.findByUsername(username) != null) {
                username = baseUsername + counter++;
            }

            // Create minimal user (only email required)
            User user = User.builder()
                    .email(email)
                    .username(username)
                    .firstName("User") // Placeholder
                    .lastName("") // Placeholder
                    .phoneNumber("+1000000000" + new Random().nextInt(9000) + 1000) // Placeholder
                    .role(studentRole)
                    .profileIncomplete(true) // Mark for later completion
                    .accountLocked(false)
                    .failedLoginAttempts(0)
                    .build();

            // Create associated student record
            Student student = new Student();
            student.setUser(user);
            user.setStudent(student);

            // Save to local database
            User savedUser = userRepository.save(user);
            log.info("Minimal user created with email: {} and marked as profile incomplete", email);

            // Create user in Cognito with temporary password
            String tempPassword = UUID.randomUUID().toString() + "Aa1!";
            try {
                String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);
                
                List<AttributeType> attributes = new ArrayList<>();
                attributes.add(AttributeType.builder().name("email").value(email).build());
                attributes.add(AttributeType.builder().name("email_verified").value("true").build());

                AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .userAttributes(attributes)
                        .temporaryPassword(tempPassword)
                        .messageAction(MessageActionType.SUPPRESS) // Don't send welcome email
                        .build();

                cognitoClient.adminCreateUser(createUserRequest);
                
                // Set permanent password
                AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .password(tempPassword)
                        .permanent(true)
                        .build();
                cognitoClient.adminSetUserPassword(setPasswordRequest);

                // Add user to student group
                AdminAddUserToGroupRequest addToGroupRequest = AdminAddUserToGroupRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .groupName("STUDENT")
                        .build();
                cognitoClient.adminAddUserToGroup(addToGroupRequest);
                
                log.info("User created in Cognito: {}", email);
            } catch (Exception e) {
                log.error("Failed to create user in Cognito: {}", email, e);
                // Continue anyway - user exists in local DB
            }

            // Generate login response without Cognito tokens for now
            return generateTokensForExistingUser(savedUser);

        } catch (Exception e) {
            log.error("Failed to create minimal user for email: {}", email, e);
            throw new CustomException("Failed to create account. Please try again.");
        }
    }

    // ============================================
    // Google OAuth Methods
    // ============================================

    @Override
    public GoogleAuthUrlResponseDto getGoogleAuthUrl(String redirectUri) {
        log.info("Generating Google OAuth URL with redirectUri: {}", redirectUri);
        
        if (cognitoDomain == null || cognitoDomain.isEmpty()) {
            throw new CustomException("Cognito domain not configured. Please configure aws.cognito.domain");
        }

        // Use provided redirectUri or fall back to configured default
        String finalRedirectUri = (redirectUri != null && !redirectUri.isEmpty()) 
            ? redirectUri 
            : googleRedirectUri;

        if (finalRedirectUri == null || finalRedirectUri.isEmpty()) {
            throw new CustomException("Google redirect URI not configured");
        }

        // Generate a random state for CSRF protection
        String state = UUID.randomUUID().toString();

        try {
            // Build Cognito Hosted UI URL for Google OAuth
            String authUrl = String.format(
                "https://%s/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&identity_provider=Google&scope=email+openid+profile&state=%s",
                cognitoDomain,
                clientId,
                URLEncoder.encode(finalRedirectUri, StandardCharsets.UTF_8),
                state
            );

            log.info("Generated Google OAuth URL: {}", authUrl);

            return GoogleAuthUrlResponseDto.builder()
                    .authUrl(authUrl)
                    .state(state)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate Google OAuth URL: {}", e.getMessage(), e);
            throw new CustomException("Failed to generate Google OAuth URL");
        }
    }

    @Override
    @Transactional
    public UserAuthLoginResponseDto handleGoogleCallback(GoogleAuthCallbackRequestDto request) {
        log.info("Handling Google OAuth callback");
        
        String code = request.getCode();
        String redirectUri = request.getRedirectUri();
        
        if (code == null || code.isEmpty()) {
            throw new CustomException("Authorization code is required");
        }

        // Use provided redirectUri or fall back to configured default
        String finalRedirectUri = (redirectUri != null && !redirectUri.isEmpty()) 
            ? redirectUri 
            : googleRedirectUri;

        try {
            // Exchange authorization code for tokens via Cognito token endpoint
            Map<String, Object> tokenResponse = exchangeCodeForTokens(code, finalRedirectUri);
            
            // Extract tokens
            String idToken = (String) tokenResponse.get("id_token");
            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            Integer expiresIn = (Integer) tokenResponse.get("expires_in");

            if (idToken == null) {
                throw new CustomException("Failed to get ID token from Google OAuth");
            }

            // Decode ID token to get user info
            DecodedJWT decodedToken = JWT.decode(idToken);
            String email = decodedToken.getClaim("email").asString();
            String firstName = decodedToken.getClaim("given_name").asString();
            String lastName = decodedToken.getClaim("family_name").asString();
            String picture = decodedToken.getClaim("picture").asString();

            if (email == null || email.isEmpty()) {
                throw new CustomException("Email not found in Google account");
            }

            email = email.toLowerCase().trim();
            log.info("Google OAuth user email: {}", email);

            // Check if user exists
            User existingUser = userRepository.findByEmail(email);
            
            if (existingUser != null) {
                // Existing user - update profile picture if available
                if (picture != null && !picture.isEmpty() && 
                    (existingUser.getProfilePicture() == null || existingUser.getProfilePicture().isEmpty())) {
                    existingUser.setProfilePicture(picture);
                    userRepository.save(existingUser);
                }
                
                log.info("Existing user login via Google OAuth: {}", email);
                return generateGoogleOAuthTokensForUser(existingUser, idToken, accessToken, refreshToken, expiresIn);
            } else {
                // New user - create account
                log.info("Creating new user via Google OAuth: {}", email);
                return createGoogleOAuthUser(email, firstName, lastName, picture, idToken, accessToken, refreshToken, expiresIn);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to handle Google OAuth callback: {}", e.getMessage(), e);
            throw new CustomException("Failed to complete Google sign-in. Please try again.");
        }
    }

    private Map<String, Object> exchangeCodeForTokens(String code, String redirectUri) {
        log.info("Exchanging authorization code for tokens");
        
        String tokenEndpoint = String.format("https://%s/oauth2/token", cognitoDomain);
        
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Add basic auth header with client credentials
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenEndpoint,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully exchanged code for tokens");
                return response.getBody();
            } else {
                throw new CustomException("Failed to exchange authorization code for tokens");
            }
        } catch (Exception e) {
            log.error("Token exchange failed: {}", e.getMessage(), e);
            throw new CustomException("Failed to exchange authorization code. Please try again.");
        }
    }

    private UserAuthLoginResponseDto createGoogleOAuthUser(
            String email, 
            String firstName, 
            String lastName, 
            String picture,
            String idToken,
            String accessToken,
            String refreshToken,
            Integer expiresIn) {
        
        // Get default student role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new CustomException("Default student role not found"));

        // Generate unique username from email (same logic as OTP)
        String baseUsername = sanitizeUsername(email.split("@")[0]);
        String username = baseUsername;
        int counter = 1;
        while (userRepository.findByUsername(username) != null) {
            username = baseUsername + counter++;
        }

        // Use Google profile data or placeholders
        String userFirstName = (firstName != null && !firstName.isEmpty()) ? firstName : "User";
        String userLastName = (lastName != null) ? lastName : "";

        // Create user
        User user = User.builder()
                .email(email)
                .username(username)
                .firstName(userFirstName)
                .lastName(userLastName)
                .phoneNumber("+1000000000" + (new Random().nextInt(9000) + 1000)) // Placeholder
                .role(studentRole)
                .profileIncomplete(true) // Mark for later completion (need phone number)
                .profilePicture(picture)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        // Create associated student record
        Student student = new Student();
        student.setUser(user);
        user.setStudent(student);

        // Save to local database
        User savedUser = userRepository.save(user);
        log.info("Google OAuth user created with email: {}, username: {}", email, username);

        // Link to Cognito (Google users are automatically created in Cognito via federation)
        try {
            // Add user to student group if not already
            AdminAddUserToGroupRequest addToGroupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .groupName("STUDENT")
                    .build();
            cognitoClient.adminAddUserToGroup(addToGroupRequest);
            log.info("Google OAuth user added to STUDENT group in Cognito: {}", email);
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException e) {
            log.error("User or group not found in Cognito for Google OAuth user: {}", email, e);
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException e) {
            log.error("Google OAuth user not found in Cognito: {}", email, e);
        } catch (Exception e) {
            log.info("User already in group or group assignment handled by federation: {} - {}", email, e.getMessage());
        }

        return generateGoogleOAuthTokensForUser(savedUser, idToken, accessToken, refreshToken, expiresIn);
    }

    private UserAuthLoginResponseDto generateGoogleOAuthTokensForUser(
            User user,
            String idToken,
            String accessToken, 
            String refreshToken,
            Integer expiresIn) {
        
        log.info("Generating login response for Google OAuth user: {}", user.getEmail());

        // Create response with Cognito tokens from Google OAuth
        UserAuthLoginResponseDto response = UserAuthLoginResponseDto.builder()
                .idToken(idToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn != null ? expiresIn : 3600)
                .build();

        // Add user details
        UserTransformer.intoUserAuthLoginRes(user, response);

        // Add permissions
        try {
            UserPermissionsResponseDto userPermissions = permissionService.getUserPermissions(user.getId());

            List<String> allPermissionNames = userPermissions.getAllPermissions().stream()
                    .map(PermissionResponseDto::getName)
                    .collect(Collectors.toList());

            Map<String, List<String>> categorizedPermissions = userPermissions.getAllPermissions().stream()
                    .filter(p -> p.getCategory() != null)
                    .collect(Collectors.groupingBy(
                            PermissionResponseDto::getCategory,
                            Collectors.mapping(PermissionResponseDto::getName, Collectors.toList())));

            UserAuthLoginResponseDto.UserPermissionInfo permissionInfo = UserAuthLoginResponseDto.UserPermissionInfo
                    .builder()
                    .roleName(user.getRole().getName())
                    .allPermissions(allPermissionNames)
                    .categories(categorizedPermissions)
                    .build();

            response.setPermissions(permissionInfo);
        } catch (Exception e) {
            log.warn("Failed to load permissions for user {}: {}", user.getId(), e.getMessage());
        }

        return response;
    }

    private String sanitizeUsername(String input) {
        // Remove special characters, keep only alphanumeric and dots/underscores
        return input.replaceAll("[^a-zA-Z0-9._]", "").toLowerCase();
    }

    @Override
    @Transactional
    public String updateUsername(Long userId, UpdateUsernameRequestDto request) {
        log.info("Updating username for userId: {}", userId);
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User with ID " + userId + " not found"));
        
        // Only allow username update for users with incomplete profiles (OAuth users)
        if (!Boolean.TRUE.equals(user.getProfileIncomplete())) {
            throw new CustomException("Username can only be changed for users with incomplete profiles");
        }
        
        String newUsername = request.getUsername().toLowerCase().trim();
        
        // Check if new username is different from current
        if (newUsername.equals(user.getUsername())) {
            return "Username is already set to this value";
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(newUsername)) {
            throw new CustomException("This username is already in use. Please choose a different one.");
        }
        
        // Update username
        user.setUsername(newUsername);
        userRepository.save(user);
        
        log.info("Username updated successfully for userId: {} to: {}", userId, newUsername);
        return "Username updated successfully";
    }

    @Override
    @Transactional
    public String updatePhoneNumber(Long userId, String phoneNumber) {
        log.info("Updating phone number for userId: {}", userId);
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User with ID " + userId + " not found"));
        
        // Check if user has a placeholder phone (can update) or real phone
        String currentPhone = user.getPhoneNumber();
        boolean hasPlaceholderPhone = currentPhone != null && currentPhone.startsWith("+1000000000");
        
        // Only allow phone update for users with placeholder phones or incomplete profiles
        if (!hasPlaceholderPhone && !Boolean.TRUE.equals(user.getProfileIncomplete())) {
            throw new CustomException("Phone number can only be updated for users with incomplete profiles");
        }
        
        // Validate and normalize the new phone number
        String normalizedPhone = phoneNumber.trim();
        if (!normalizedPhone.matches("^\\+?[0-9]{10,15}$")) {
            throw new CustomException("Invalid phone number format. Must be 10-15 digits with optional + prefix");
        }
        
        // Check if new phone is different from current
        if (normalizedPhone.equals(currentPhone)) {
            return "Phone number is already set to this value";
        }
        
        // Check if phone number already exists for another user
        User existingUser = userRepository.findByPhoneNumber(normalizedPhone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new CustomException("This phone number is already in use by another account");
        }
        
        // Update phone number in local database
        user.setPhoneNumber(normalizedPhone);
        
        // If user now has real phone, check if profile should be marked complete
        // Only mark complete if they also have a real username (not auto-generated)
        if (hasPlaceholderPhone && Boolean.TRUE.equals(user.getProfileIncomplete())) {
            // Keep profileIncomplete true - let user manually complete profile
            // or add additional logic here to check other required fields
            log.info("User {} updated phone from placeholder, profileIncomplete still true", userId);
        }
        
        userRepository.save(user);
        
        // Optionally update in Cognito as well
        try {
            AdminUpdateUserAttributesRequest cognitoRequest = AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getEmail())
                    .userAttributes(
                            AttributeType.builder()
                                    .name("phone_number")
                                    .value(normalizedPhone)
                                    .build()
                    )
                    .build();
            cognitoClient.adminUpdateUserAttributes(cognitoRequest);
            log.info("Phone number updated in Cognito for user: {}", user.getEmail());
        } catch (Exception e) {
            // Log but don't fail - local DB is the source of truth
            log.warn("Failed to update phone in Cognito for user {}: {}", user.getEmail(), e.getMessage());
        }
        
        log.info("Phone number updated successfully for userId: {}", userId);
        return "Phone number updated successfully";
    }
}

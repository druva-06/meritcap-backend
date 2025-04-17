package com.consultancy.education.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthLoginRequestDto;
import com.consultancy.education.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.consultancy.education.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.User;
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
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    UserAuthServiceImpl(UserRepository userRepository, CognitoIdentityProviderClient cognitoClient) {
        this.userRepository = userRepository;
        this.cognitoClient = cognitoClient;
    }

    @Override
    public String signup(UserAuthSignUpRequestDto userAuthSignUpRequestDto) {
        log.info("Signup service started");

        Map<String, AttributeType> attributes = new HashMap<>();
        attributes.put("email", AttributeType.builder().name("email").value(userAuthSignUpRequestDto.getEmail()).build());
        attributes.put("phone_number", AttributeType.builder().name("phone_number").value(userAuthSignUpRequestDto.getPhoneNumber()).build());
        attributes.put("name", AttributeType.builder().name("name").value(userAuthSignUpRequestDto.getFirstName()).build());
        attributes.put("preferred_username", AttributeType.builder().name("preferred_username").value(userAuthSignUpRequestDto.getUsername()).build());
        attributes.put("custom:role", AttributeType.builder().name("custom:role").value(String.valueOf(userAuthSignUpRequestDto.getRole())).build()); // Store role as custom attribute

        String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, userAuthSignUpRequestDto.getEmail());

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .username(userAuthSignUpRequestDto.getEmail())
                .password(userAuthSignUpRequestDto.getPassword())
                .userAttributes(attributes.values())
                .secretHash(secretHash)
                .build();

        log.info("SignupRequest: {}", signUpRequest);

        try{
            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            User user = UserAuthTransformer.toUserEntity(userAuthSignUpRequestDto);
            Student student = new Student();
            student.setUser(user);
            user.setStudent(student);
            userRepository.save(user);
            log.info("Signup response: {}", response);
            return "User Registered Successfully!";
        }
        catch (CognitoIdentityProviderException e){
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.error("Signup error: {}", errorMessage);

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
        }
    }

    @Override
    public UserAuthLoginResponseDto login(UserAuthLoginRequestDto userAuthLoginRequestDto){
        log.info("Login service started");

        UserAuthLoginResponseDto userAuthLoginResponseDto = new UserAuthLoginResponseDto();
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userAuthLoginRequestDto.getEmail());
        authParams.put("PASSWORD", userAuthLoginRequestDto.getPassword());
        authParams.put("SECRET_HASH", CognitoUtil.calculateSecretHash(clientId, clientSecret, userAuthLoginRequestDto.getEmail()));

        log.info("AuthParams {}", authParams);
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();

        try {
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            userAuthLoginResponseDto =  UserAuthTransformer.toLoginResDto(authResponse);
            DecodedJWT jwt = JWT.decode(userAuthLoginResponseDto.getIdToken());
            String email = jwt.getClaim("email").asString();
            if(email == null){
                throw new CustomException("Invalid JWT Token.");
            }
            User user = userRepository.findByEmail(email);
            UserTransformer.intoUserAuthLoginRes(user, userAuthLoginResponseDto);
            return userAuthLoginResponseDto;
        }
        catch (CognitoIdentityProviderException e){
            String errorMessage = e.awsErrorDetails().errorMessage();
            log.error("Login error: {}", errorMessage);
            if (errorMessage.contains("User is not confirmed")) {
                throw new CustomException("User account is not confirmed. Please verify your email.");
            } else if (errorMessage.contains("Incorrect username or password")) {
                throw new CustomException("Incorrect email or password.");
            } else if (errorMessage.contains("User does not exist")) {
                throw new CustomException("No account found with this email.");
            } else {
                throw new CustomException(errorMessage);
            }
        }
    }

    @Override
    public String resendVerificationCode(String email) {
        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ResendConfirmationCodeRequest resendRequest = ResendConfirmationCodeRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .secretHash(secretHash)
                    .build();
            cognitoClient.resendConfirmationCode(resendRequest);
            return "Verification code has been resent to your email.";
        } catch (CognitoIdentityProviderException e) {
            log.error("Error resending verification code: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public String confirmVerificationCode(String email, String verificationCode) {
        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(verificationCode)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.confirmSignUp(confirmRequest);
            return "Account successfully verified.";
        } catch (CognitoIdentityProviderException e) {
            log.error("Error confirming verification code: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public String forgotPassword(String email) {
        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ForgotPasswordRequest forgotRequest = ForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .secretHash(secretHash)
                    .build();

            cognitoClient.forgotPassword(forgotRequest);
            return "Password reset code has been sent to your email.";
        } catch (CognitoIdentityProviderException e) {
            log.error("Error requesting password reset: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public String confirmForgotPassword(String email, String confirmationCode, String newPassword) {
        try {
            String secretHash = CognitoUtil.calculateSecretHash(clientId, clientSecret, email);

            ConfirmForgotPasswordRequest confirmRequest = ConfirmForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .password(newPassword) // New password
                    .secretHash(secretHash) // Include SECRET_HASH
                    .build();

            cognitoClient.confirmForgotPassword(confirmRequest);
            return "Password has been successfully reset.";
        } catch (CognitoIdentityProviderException e) {
            log.error("Error confirming password reset: {}", e.awsErrorDetails().errorMessage());
            throw new CustomException(e.awsErrorDetails().errorMessage());
        }
    }
}

package com.meritcap.transformer;

import com.meritcap.DTOs.requestDTOs.userAuth.UserAuthSignUpRequestDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.meritcap.model.Role;
import com.meritcap.model.User;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;

@Slf4j
public class UserAuthTransformer {
    public static UserAuthLoginResponseDto toLoginResDto(InitiateAuthResponse authResponse) {
        log.info("UserAuthTransformer toLoginResDto");

        return UserAuthLoginResponseDto.builder()
                .accessToken(authResponse.authenticationResult().accessToken())
                .idToken(authResponse.authenticationResult().idToken())
                .refreshToken(authResponse.authenticationResult().refreshToken())
                .expiresIn(authResponse.authenticationResult().expiresIn())
                .tokenType(authResponse.authenticationResult().tokenType())
                .build();
    }

    public static UserAuthLoginResponseDto toAdminLoginResDto(AdminInitiateAuthResponse authResponse) {
        log.info("UserAuthTransformer toAdminLoginResDto");

        return UserAuthLoginResponseDto.builder()
                .accessToken(authResponse.authenticationResult().accessToken())
                .idToken(authResponse.authenticationResult().idToken())
                .refreshToken(authResponse.authenticationResult().refreshToken())
                .expiresIn(authResponse.authenticationResult().expiresIn())
                .tokenType(authResponse.authenticationResult().tokenType())
                .build();
    }

    public static User toUserEntity(UserAuthSignUpRequestDto userAuthSignUpRequestDto, Role role) {
        log.info("UserAuthSignUpRequestDto toUserEntity");
        return User.builder()
                .firstName(userAuthSignUpRequestDto.getFirstName())
                .lastName(userAuthSignUpRequestDto.getLastName())
                .username(userAuthSignUpRequestDto.getUsername())
                .email(userAuthSignUpRequestDto.getEmail())
                .phoneNumber(userAuthSignUpRequestDto.getPhoneNumber())
                .profilePicture(userAuthSignUpRequestDto.getProfilePicture())
                .role(role)
                .build();
    }
}

package com.meritcap.transformer;

import com.meritcap.DTOs.responseDTOs.user.UserResponseDto;
import com.meritcap.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.meritcap.model.User;

public class UserTransformer {
    public static void intoUserAuthLoginRes(User user, UserAuthLoginResponseDto userAuthLoginResponseDto) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserId(user.getId());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFirstName(user.getFirstName());
        userResponseDto.setLastName(user.getLastName());
        userResponseDto.setPhoneNumber(user.getPhoneNumber());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        userResponseDto.setProfilePicture(user.getProfilePicture());
        userResponseDto.setProfileIncomplete(user.getProfileIncomplete());
        userAuthLoginResponseDto.setUser(userResponseDto);
    }

    public static UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .profileIncomplete(user.getProfileIncomplete())
                .build();
    }

    // public static User toEntity(UserRequestDto userRequestDto) {
    // return User.builder()
    // .name(userRequestDto.getName())
    // .email(userRequestDto.getEmail())
    // .phoneNumber(userRequestDto.getPhoneNumber())
    // .type(userRequestDto.getType())
    // .build();
    // }
    //
    // public static UserResponseDto toResDTO(User user) {
    // return UserResponseDto.builder()
    // .userId(user.getId())
    // .name(user.getName())
    // .email(user.getEmail())
    // .phoneNumber(user.getPhoneNumber())
    // .type(user.getType())
    // .build();
    // }
    //
    // public static void updateUser(User user, UserRequestDto userRequestDto) {
    // user.setName(userRequestDto.getName());
    // user.setEmail(userRequestDto.getEmail());
    // user.setPhoneNumber(userRequestDto.getPhoneNumber());
    // user.setType(userRequestDto.getType());
    // }
}

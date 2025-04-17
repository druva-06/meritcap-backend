package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import com.consultancy.education.DTOs.responseDTOs.userAuth.UserAuthLoginResponseDto;
import com.consultancy.education.model.User;

public class UserTransformer {
    public static void intoUserAuthLoginRes(User user, UserAuthLoginResponseDto userAuthLoginResponseDto) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserId(user.getId());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFirstName(user.getFirstName());
        userResponseDto.setLastName(user.getLastName());
        userResponseDto.setPhoneNumber(user.getPhoneNumber());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setRole(user.getRole());
        userResponseDto.setProfilePicture(user.getProfilePicture());
        userAuthLoginResponseDto.setUser(userResponseDto);
    }

//    public static User toEntity(UserRequestDto userRequestDto) {
//        return User.builder()
//                .name(userRequestDto.getName())
//                .email(userRequestDto.getEmail())
//                .phoneNumber(userRequestDto.getPhoneNumber())
//                .type(userRequestDto.getType())
//                .build();
//    }
//
//    public static UserResponseDto toResDTO(User user) {
//        return UserResponseDto.builder()
//                .userId(user.getId())
//                .name(user.getName())
//                .email(user.getEmail())
//                .phoneNumber(user.getPhoneNumber())
//                .type(user.getType())
//                .build();
//    }
//
//    public static void updateUser(User user, UserRequestDto userRequestDto) {
//        user.setName(userRequestDto.getName());
//        user.setEmail(userRequestDto.getEmail());
//        user.setPhoneNumber(userRequestDto.getPhoneNumber());
//        user.setType(userRequestDto.getType());
//    }
}

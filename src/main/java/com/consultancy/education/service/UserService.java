package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.user.UserRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto addUser(UserRequestDto userRequestDto);

    UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId);

    UserResponseDto getUser(Long userId);

    String uploadFile(Long userId, String documentType, MultipartFile file);
}

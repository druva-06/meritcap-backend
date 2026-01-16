package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.user.UserRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.CounselorDto;
import com.consultancy.education.DTOs.responseDTOs.user.PagedUserResponseDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import com.consultancy.education.model.Role;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponseDto addUser(UserRequestDto userRequestDto);

    UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId);

    UserResponseDto getUser(Long userId);

    String uploadFile(Long userId, String documentType, MultipartFile file);

    List<CounselorDto> getAllCounselors();

    PagedUserResponseDto getUsersByRole(Role role, int page, int size);

    PagedUserResponseDto getUsersByRoleName(String roleName, int page, int size);

    PagedUserResponseDto getAllUsers(int page, int size, String search);

    void deleteUser(Long userId);

    UserResponseDto updateUserRole(Long userId, String roleName);
}

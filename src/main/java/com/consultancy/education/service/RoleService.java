package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.role.RoleRequestDto;
import com.consultancy.education.DTOs.responseDTOs.role.RoleResponseDto;

import java.util.List;

public interface RoleService {

    RoleResponseDto createRole(RoleRequestDto requestDto, Long createdBy);

    RoleResponseDto updateRole(Long roleId, RoleRequestDto requestDto, Long updatedBy);

    void deleteRole(Long roleId, Long deletedBy);

    RoleResponseDto getRoleById(Long roleId);

    RoleResponseDto getRoleByName(String name);

    List<RoleResponseDto> getAllRoles();

    List<RoleResponseDto> getActiveRoles();
}

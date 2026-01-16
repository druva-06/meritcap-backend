package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.role.RoleRequestDto;
import com.consultancy.education.DTOs.responseDTOs.role.RoleResponseDto;
import com.consultancy.education.model.Role;

public class RoleTransformer {

    public static Role requestDtoToRole(RoleRequestDto requestDto) {
        return Role.builder()
                .name(requestDto.getName().toUpperCase())
                .displayName(requestDto.getDisplayName())
                .description(requestDto.getDescription())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .isSystemRole(requestDto.getIsSystemRole() != null ? requestDto.getIsSystemRole() : false)
                .build();
    }

    public static RoleResponseDto roleToResponseDto(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .isSystemRole(role.getIsSystemRole())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    public static void updateRoleFromDto(Role role, RoleRequestDto requestDto) {
        if (requestDto.getName() != null) {
            role.setName(requestDto.getName().toUpperCase());
        }
        if (requestDto.getDisplayName() != null) {
            role.setDisplayName(requestDto.getDisplayName());
        }
        if (requestDto.getDescription() != null) {
            role.setDescription(requestDto.getDescription());
        }
        if (requestDto.getIsActive() != null) {
            role.setIsActive(requestDto.getIsActive());
        }
    }
}

package com.consultancy.education.DTOs.responseDTOs.user;

import com.consultancy.education.DTOs.responseDTOs.permission.PermissionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPermissionsResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String roleName;
    private Set<PermissionResponseDto> rolePermissions;
    private Set<PermissionResponseDto> additionalPermissions;
    private Set<PermissionResponseDto> allPermissions;
}

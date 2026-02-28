package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.permission.AssignPermissionsRequestDto;
import com.consultancy.education.DTOs.requestDTOs.permission.PermissionRequestDto;
import com.consultancy.education.DTOs.responseDTOs.permission.PermissionHierarchyDto;
import com.consultancy.education.DTOs.responseDTOs.permission.PermissionResponseDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserPermissionsResponseDto;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    PermissionResponseDto createPermission(PermissionRequestDto requestDto);

    PermissionResponseDto updatePermission(Long permissionId, PermissionRequestDto requestDto);

    void deletePermission(Long permissionId);

    PermissionResponseDto getPermissionById(Long permissionId);

    PermissionResponseDto getPermissionByName(String name);

    List<PermissionResponseDto> getAllPermissions();

    List<PermissionResponseDto> getActivePermissions();

    List<PermissionResponseDto> getPermissionsByCategory(String category);

    // Hierarchy methods
    List<String> getAllDashboards();

    List<String> getSubmenusByDashboard(String dashboard);

    List<String> getFeaturesByDashboardAndSubmenu(String dashboard, String submenu);

    List<PermissionHierarchyDto> getPermissionHierarchy();

    Set<PermissionResponseDto> getPermissionsByRoleId(Long roleId);

    UserPermissionsResponseDto getUserPermissions(Long userId);

    void assignPermissionsToUser(AssignPermissionsRequestDto requestDto);

    void revokePermissionsFromUser(Long userId, Set<Long> permissionIds);

    void assignPermissionsToRole(Long roleId, Set<Long> permissionIds);

    void revokePermissionsFromRole(Long roleId, Set<Long> permissionIds);
}

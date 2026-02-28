package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.permission.AssignPermissionsRequestDto;
import com.consultancy.education.DTOs.requestDTOs.permission.PermissionRequestDto;
import com.consultancy.education.DTOs.responseDTOs.permission.PermissionHierarchyDto;
import com.consultancy.education.DTOs.responseDTOs.permission.PermissionResponseDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserPermissionsResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.BadRequestException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.Permission;
import com.consultancy.education.model.Role;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.PermissionRepository;
import com.consultancy.education.repository.RoleRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.service.PermissionService;
import com.consultancy.education.transformer.PermissionTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

        @Autowired
        private PermissionRepository permissionRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Override
        @Transactional
        public PermissionResponseDto createPermission(PermissionRequestDto requestDto) {
                log.info("Creating new permission: {}", requestDto.getName());

                if (permissionRepository.existsByNameIgnoreCase(requestDto.getName())) {
                        throw new AlreadyExistException(
                                        List.of("Permission with name '" + requestDto.getName() + "' already exists"));
                }

                Permission permission = PermissionTransformer.requestDtoToPermission(requestDto);
                Permission savedPermission = permissionRepository.save(permission);
                log.info("Permission created successfully with ID: {}", savedPermission.getId());

                return PermissionTransformer.permissionToResponseDto(savedPermission);
        }

        @Override
        @Transactional
        public PermissionResponseDto updatePermission(Long permissionId, PermissionRequestDto requestDto) {
                log.info("Updating permission with ID: {}", permissionId);

                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Permission not found with ID: " + permissionId));

                if (requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(permission.getName())) {
                        if (permissionRepository.existsByNameIgnoreCase(requestDto.getName())) {
                                throw new AlreadyExistException(
                                                List.of("Permission with name '" + requestDto.getName()
                                                                + "' already exists"));
                        }
                }

                PermissionTransformer.updatePermissionFromDto(permission, requestDto);
                Permission updatedPermission = permissionRepository.save(permission);
                log.info("Permission updated successfully: {}", permissionId);

                return PermissionTransformer.permissionToResponseDto(updatedPermission);
        }

        @Override
        @Transactional
        public void deletePermission(Long permissionId) {
                log.info("Deleting permission with ID: {}", permissionId);

                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Permission not found with ID: " + permissionId));

                permissionRepository.delete(permission);
                log.info("Permission deleted successfully: {}", permissionId);
        }

        @Override
        public PermissionResponseDto getPermissionById(Long permissionId) {
                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Permission not found with ID: " + permissionId));
                return PermissionTransformer.permissionToResponseDto(permission);
        }

        @Override
        public PermissionResponseDto getPermissionByName(String name) {
                Permission permission = permissionRepository.findByNameIgnoreCase(name)
                                .orElseThrow(() -> new NotFoundException("Permission not found with name: " + name));
                return PermissionTransformer.permissionToResponseDto(permission);
        }

        @Override
        public List<PermissionResponseDto> getAllPermissions() {
                return permissionRepository.findAll().stream()
                                .map(PermissionTransformer::permissionToResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<PermissionResponseDto> getActivePermissions() {
                return permissionRepository.findByIsActiveTrue().stream()
                                .map(PermissionTransformer::permissionToResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<PermissionResponseDto> getPermissionsByCategory(String category) {
                return permissionRepository.findByCategoryAndIsActiveTrue(category.toUpperCase()).stream()
                                .map(PermissionTransformer::permissionToResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        public Set<PermissionResponseDto> getPermissionsByRoleId(Long roleId) {
                return permissionRepository.findPermissionsByRoleId(roleId).stream()
                                .map(PermissionTransformer::permissionToResponseDto)
                                .collect(Collectors.toSet());
        }

        @Override
        public UserPermissionsResponseDto getUserPermissions(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

                Role role = user.getRole();

                // Get role permissions
                Set<Permission> rolePermissions = permissionRepository.findPermissionsByRoleId(role.getId());

                // Get additional user permissions
                Set<Permission> additionalPermissions = permissionRepository.findAdditionalPermissionsByUserId(userId);

                // Combine all permissions
                Set<Permission> allPermissions = new HashSet<>(rolePermissions);
                allPermissions.addAll(additionalPermissions);

                return UserPermissionsResponseDto.builder()
                                .userId(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .roleName(role.getName())
                                .rolePermissions(rolePermissions.stream()
                                                .map(PermissionTransformer::permissionToResponseDto)
                                                .collect(Collectors.toSet()))
                                .additionalPermissions(additionalPermissions.stream()
                                                .map(PermissionTransformer::permissionToResponseDto)
                                                .collect(Collectors.toSet()))
                                .allPermissions(allPermissions.stream()
                                                .map(PermissionTransformer::permissionToResponseDto)
                                                .collect(Collectors.toSet()))
                                .build();
        }

        @Override
        @Transactional
        public void assignPermissionsToUser(AssignPermissionsRequestDto requestDto) {
                log.info("Assigning permissions to user ID: {}", requestDto.getUserId());

                User user = userRepository.findById(requestDto.getUserId())
                                .orElseThrow(() -> new NotFoundException(
                                                "User not found with ID: " + requestDto.getUserId()));

                Set<Permission> permissions = new HashSet<>();
                for (Long permissionId : requestDto.getPermissionIds()) {
                        Permission permission = permissionRepository.findById(permissionId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Permission not found with ID: " + permissionId));
                        permissions.add(permission);
                }

                user.getAdditionalPermissions().addAll(permissions);
                userRepository.save(user);
                log.info("Permissions assigned successfully to user: {}", requestDto.getUserId());
        }

        @Override
        @Transactional
        public void revokePermissionsFromUser(Long userId, Set<Long> permissionIds) {
                log.info("Revoking permissions from user ID: {}", userId);

                // Eagerly fetch user with permissions to ensure collection is initialized
                User user = userRepository.findByIdWithPermissions(userId);
                if (user == null) {
                        throw new NotFoundException("User not found with ID: " + userId);
                }

                // Remove permissions by iterating to ensure proper JPA collection management
                boolean removed = user.getAdditionalPermissions()
                                .removeIf(permission -> permissionIds.contains(permission.getId()));

                if (removed) {
                        userRepository.save(user);
                        log.info("Permissions revoked successfully from user: {}", userId);
                } else {
                        log.info("No matching permissions found to revoke for user: {}", userId);
                }
        }

        @Override
        @Transactional
        public void assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
                log.info("Assigning permissions to role ID: {}", roleId);

                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

                Set<Permission> permissions = new HashSet<>();
                for (Long permissionId : permissionIds) {
                        Permission permission = permissionRepository.findById(permissionId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        "Permission not found with ID: " + permissionId));
                        permissions.add(permission);
                }

                role.getPermissions().addAll(permissions);
                roleRepository.save(role);
                log.info("Permissions assigned successfully to role: {}", roleId);
        }

        @Override
        @Transactional
        public void revokePermissionsFromRole(Long roleId, Set<Long> permissionIds) {
                log.info("Revoking permissions from role ID: {}", roleId);

                // Eagerly fetch role with permissions to ensure collection is initialized
                Role role = roleRepository.findByIdWithPermissions(roleId);
                if (role == null) {
                        throw new NotFoundException("Role not found with ID: " + roleId);
                }

                // Remove permissions by iterating to ensure proper JPA collection management
                boolean removed = role.getPermissions()
                                .removeIf(permission -> permissionIds.contains(permission.getId()));

                if (removed) {
                        roleRepository.save(role);
                        log.info("Permissions revoked successfully from role: {}", roleId);
                } else {
                        log.info("No matching permissions found to revoke for role: {}", roleId);
                }
        }

        // ============================================
        // HIERARCHY METHODS
        // ============================================

        @Override
        public List<String> getAllDashboards() {
                log.debug("Fetching all unique dashboards");
                return permissionRepository.findAll().stream()
                                .map(Permission::getDashboard)
                                .filter(Objects::nonNull)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList());
        }

        @Override
        public List<String> getSubmenusByDashboard(String dashboard) {
                log.debug("Fetching submenus for dashboard: {}", dashboard);
                return permissionRepository.findAll().stream()
                                .filter(p -> dashboard.equals(p.getDashboard()))
                                .map(Permission::getSubmenu)
                                .filter(Objects::nonNull)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList());
        }

        @Override
        public List<String> getFeaturesByDashboardAndSubmenu(String dashboard, String submenu) {
                log.debug("Fetching features for dashboard: {} and submenu: {}", dashboard, submenu);
                return permissionRepository.findAll().stream()
                                .filter(p -> dashboard.equals(p.getDashboard()))
                                .filter(p -> submenu == null ? p.getSubmenu() == null
                                                : submenu.equals(p.getSubmenu()))
                                .map(Permission::getFeature)
                                .filter(Objects::nonNull)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList());
        }

        @Override
        public List<PermissionHierarchyDto> getPermissionHierarchy() {
                log.info("Building complete permission hierarchy");

                List<Permission> allPermissions = permissionRepository.findAll();

                // Group by dashboard
                Map<String, List<Permission>> dashboardMap = allPermissions.stream()
                                .filter(p -> p.getDashboard() != null)
                                .collect(Collectors.groupingBy(Permission::getDashboard));

                List<PermissionHierarchyDto> hierarchy = new ArrayList<>();

                for (Map.Entry<String, List<Permission>> dashboardEntry : dashboardMap.entrySet()) {
                        String dashboard = dashboardEntry.getKey();
                        List<Permission> dashboardPermissions = dashboardEntry.getValue();

                        // Group by submenu
                        Map<String, List<Permission>> submenuMap = dashboardPermissions.stream()
                                        .collect(Collectors.groupingBy(p -> p.getSubmenu() != null ? p.getSubmenu()
                                                        : ""));

                        List<PermissionHierarchyDto.SubmenuDto> submenus = new ArrayList<>();

                        for (Map.Entry<String, List<Permission>> submenuEntry : submenuMap.entrySet()) {
                                String submenu = submenuEntry.getKey();
                                List<Permission> submenuPermissions = submenuEntry.getValue();

                                List<PermissionHierarchyDto.FeatureDto> features = submenuPermissions.stream()
                                                .map(p -> PermissionHierarchyDto.FeatureDto.builder()
                                                                .feature(p.getFeature())
                                                                .permissionId(p.getId())
                                                                .permissionName(p.getName())
                                                                .displayName(p.getDisplayName())
                                                                .isActive(p.getIsActive())
                                                                .build())
                                                .sorted(Comparator.comparing(
                                                                PermissionHierarchyDto.FeatureDto::getFeature))
                                                .collect(Collectors.toList());

                                submenus.add(PermissionHierarchyDto.SubmenuDto.builder()
                                                .submenu(submenu.isEmpty() ? null : submenu)
                                                .features(features)
                                                .build());
                        }

                        // Sort submenus - nulls first (main menu items), then alphabetically
                        submenus.sort(Comparator.comparing(
                                        PermissionHierarchyDto.SubmenuDto::getSubmenu,
                                        Comparator.nullsFirst(Comparator.naturalOrder())));

                        hierarchy.add(PermissionHierarchyDto.builder()
                                        .dashboard(dashboard)
                                        .submenus(submenus)
                                        .build());
                }

                // Sort by dashboard name
                hierarchy.sort(Comparator.comparing(PermissionHierarchyDto::getDashboard));

                log.info("Permission hierarchy built successfully with {} dashboards", hierarchy.size());
                return hierarchy;
        }
}

package com.consultancy.education.DTOs.responseDTOs.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO representing the hierarchical structure of permissions
 * Dashboard → Submenu → Features
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionHierarchyDto {
    private String dashboard;
    private List<SubmenuDto> submenus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubmenuDto {
        private String submenu;
        private List<FeatureDto> features;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FeatureDto {
        private String feature;
        private Long permissionId;
        private String permissionName;
        private String displayName;
        private Boolean isActive;
    }
}

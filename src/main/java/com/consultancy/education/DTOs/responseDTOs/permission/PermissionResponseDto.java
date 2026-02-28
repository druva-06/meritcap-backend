package com.consultancy.education.DTOs.responseDTOs.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionResponseDto {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String category;
    private String dashboard;
    private String submenu;
    private String feature;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

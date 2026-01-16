package com.consultancy.education.DTOs.responseDTOs.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponseDto {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Boolean isActive;
    private Boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}

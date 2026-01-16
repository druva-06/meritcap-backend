package com.consultancy.education.DTOs.requestDTOs.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignPermissionsRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one permission ID is required")
    private Set<Long> permissionIds;
}

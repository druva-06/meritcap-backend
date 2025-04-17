package com.consultancy.education.DTOs.responseDTOs.user;

import com.consultancy.education.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "User response DTO containing user details.")
public class UserResponseDto {
    @Schema(description = "Unique user identifier")
    Long userId;
    @Schema(description = "User firstname")
    String firstName;
    @Schema(description = "User lastname")
    String lastName;
    @Schema(description = "Username")
    String username;
    @Schema(description = "User email")
    String email;
    @Schema(description = "User phoneNumber")
    String phoneNumber;
    @Schema(description = "User profile picture")
    String profilePicture;
    @Schema(description = "User Role")
    Role role;
}

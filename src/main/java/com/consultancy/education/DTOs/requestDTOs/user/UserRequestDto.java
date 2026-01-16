package com.consultancy.education.DTOs.requestDTOs.user;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserRequestDto {
    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    String phoneNumber;

    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid profile picture URL")
    String profilePicture;

    @NotBlank(message = "Role name is required")
    String roleName;
}

package com.meritcap.DTOs.requestDTOs.userAuth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "User signup request dto api request")
public class UserAuthSignUpRequestDto {

    @NotBlank(message = "FirstName is required")
    String firstName;

    @NotBlank(message = "LastName is required")
    String lastName;

    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email;

    @NotBlank(message = "Password is required")
    String password;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits with optional + prefix")
    String phoneNumber;

    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid profile picture URL")
    String profilePicture;

    @NotBlank(message = "Role is required")
    String role;
}

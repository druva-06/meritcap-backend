package com.meritcap.DTOs.requestDTOs.userAuth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Google OAuth callback request DTO")
public class GoogleAuthCallbackRequestDto {

    @NotBlank(message = "Authorization code is required")
    @Schema(description = "Authorization code received from Google OAuth callback")
    String code;

    @Schema(description = "Redirect URI used during OAuth flow")
    String redirectUri;

    @Schema(description = "Optional state parameter for CSRF protection")
    String state;
}

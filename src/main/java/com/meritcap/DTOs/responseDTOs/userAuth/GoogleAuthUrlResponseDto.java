package com.meritcap.DTOs.responseDTOs.userAuth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Google OAuth URL response DTO")
public class GoogleAuthUrlResponseDto {

    @Schema(description = "Full URL to redirect user for Google OAuth")
    String authUrl;

    @Schema(description = "State parameter for CSRF protection")
    String state;
}

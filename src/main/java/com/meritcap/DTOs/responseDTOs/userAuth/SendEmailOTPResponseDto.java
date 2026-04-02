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
@Schema(description = "Response DTO for OTP send operation")
public class SendEmailOTPResponseDto {
    
    @Schema(description = "Success status", example = "true")
    Boolean success;
    
    @Schema(description = "Message to display to user", example = "OTP sent successfully to your email")
    String message;
    
    @Schema(description = "Expiry time in seconds", example = "600")
    Integer expiresIn;
}

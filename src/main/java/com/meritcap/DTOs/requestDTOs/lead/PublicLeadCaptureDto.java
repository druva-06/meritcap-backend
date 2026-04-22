package com.meritcap.DTOs.requestDTOs.lead;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicLeadCaptureDto {

    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    String phone;

    @Email(message = "Invalid email address")
    String email;

    @NotNull(message = "Campaign ID is required")
    Long campaignId;
}

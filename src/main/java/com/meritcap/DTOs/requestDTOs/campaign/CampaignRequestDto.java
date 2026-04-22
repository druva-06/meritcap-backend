package com.meritcap.DTOs.requestDTOs.campaign;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignRequestDto {

    @NotBlank(message = "Campaign name is required")
    @Schema(description = "Unique campaign name")
    String name;

    @NotBlank(message = "Source is required")
    @Schema(description = "Lead source (e.g. Offline Event, Google Ads)")
    String source;

    @Schema(description = "Optional description")
    String description;

    @Schema(description = "QR code URL generated on the frontend")
    String qrCode;
}

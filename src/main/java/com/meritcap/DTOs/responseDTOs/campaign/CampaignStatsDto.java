package com.meritcap.DTOs.responseDTOs.campaign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignStatsDto {

    @Schema(description = "Campaign ID")
    Long id;

    @Schema(description = "Campaign name")
    String name;

    @Schema(description = "Lead source")
    String source;

    @Schema(description = "Campaign description")
    String description;

    @Schema(description = "QR code URL")
    String qrCode;

    @Schema(description = "Campaign status: ACTIVE, COMPLETED, PAUSED")
    String status;

    @Schema(description = "Created by (user email)")
    String createdBy;

    @Schema(description = "Creation date")
    String createdDate;

    @Schema(description = "Total leads tagged with this campaign")
    long totalLeads;

    @Schema(description = "Leads assigned to a counselor")
    long assignedLeads;

    @Schema(description = "Leads not yet assigned to any counselor")
    long unassignedLeads;

    @Schema(description = "Leads flagged as duplicates within this campaign")
    long duplicateLeads;
}

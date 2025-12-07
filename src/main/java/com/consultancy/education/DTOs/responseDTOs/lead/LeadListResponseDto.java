package com.consultancy.education.DTOs.responseDTOs.lead;

import com.consultancy.education.enums.LeadStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Lead list item DTO - minimal info for table view")
public class LeadListResponseDto {

    @Schema(description = "Lead ID")
    Long id;

    @Schema(description = "Full name")
    String name;

    @Schema(description = "Email address")
    String email;

    @Schema(description = "Phone number")
    String phoneNumber;

    @Schema(description = "Lead status")
    LeadStatus status;

    @Schema(description = "Lead score (0-100)")
    Integer score;

    @Schema(description = "Tags")
    List<String> tags;

    @Schema(description = "Assigned counselor name")
    String assignedToName;

    @Schema(description = "Is duplicate")
    Boolean isDuplicate;

    @Schema(description = "Created timestamp")
    LocalDateTime createdAt;
}

package com.consultancy.education.DTOs.requestDTOs.lead;

import com.consultancy.education.enums.LeadStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Lead filter criteria for search and filtering")
public class LeadFilterDto {
    
    @Schema(description = "Search by name, email, or phone number")
    String search;

    @Schema(description = "Filter by campaign name")
    String campaign;

    @Schema(description = "Filter by date from (format: yyyy-MM-dd)")
    LocalDate dateFrom;

    @Schema(description = "Filter by date to (format: yyyy-MM-dd)")
    LocalDate dateTo;

    @Schema(description = "Filter by score from (0-100)")
    Integer scoreFrom;

    @Schema(description = "Filter by score to (0-100)")
    Integer scoreTo;

    @Schema(description = "Filter by status list")
    List<LeadStatus> status;

    @Schema(description = "Filter by tags")
    List<String> tags;

    @Schema(description = "Filter by assigned counselor ID")
    Long assignedTo;

    @Schema(description = "Page number (0-based)")
    @Builder.Default
    Integer page = 0;

    @Schema(description = "Page size")
    @Builder.Default
    Integer size = 10;

    @Schema(description = "Sort field (e.g., createdAt, score, firstName)")
    @Builder.Default
    String sortBy = "createdAt";

    @Schema(description = "Sort direction (ASC or DESC)")
    @Builder.Default
    String sortDirection = "DESC";
}

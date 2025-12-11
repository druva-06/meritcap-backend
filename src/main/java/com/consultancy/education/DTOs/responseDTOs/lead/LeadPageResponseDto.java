package com.consultancy.education.DTOs.responseDTOs.lead;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Paginated lead response")
public class LeadPageResponseDto {
    
    @Schema(description = "List of leads")
    List<LeadListResponseDto> leads;

    @Schema(description = "Current page number (0-based)")
    Integer currentPage;

    @Schema(description = "Page size")
    Integer pageSize;

    @Schema(description = "Total number of elements")
    Long totalElements;

    @Schema(description = "Total number of pages")
    Integer totalPages;

    @Schema(description = "Is this the first page")
    Boolean isFirst;

    @Schema(description = "Is this the last page")
    Boolean isLast;

    @Schema(description = "Has next page")
    Boolean hasNext;

    @Schema(description = "Has previous page")
    Boolean hasPrevious;
}

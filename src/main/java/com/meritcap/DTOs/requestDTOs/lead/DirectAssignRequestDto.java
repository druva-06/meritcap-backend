package com.meritcap.DTOs.requestDTOs.lead;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectAssignRequestDto {

    // null = pick from all unassigned leads across all campaigns
    private String campaignName;

    @NotNull(message = "Counselor ID is required")
    private Long counselorId;

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    private Integer count;

    // "score" = highest score first; "createdAt" = oldest first (default FIFO)
    private String sortBy = "createdAt";
}

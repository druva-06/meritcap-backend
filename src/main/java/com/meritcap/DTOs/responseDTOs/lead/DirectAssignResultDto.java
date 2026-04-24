package com.meritcap.DTOs.responseDTOs.lead;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectAssignResultDto {
    private int assignedCount;    // actual leads assigned (may be < requestedCount if not enough available)
    private int requestedCount;
    private String counselorName;
    private String campaignName;  // null when no campaign filter was applied
}

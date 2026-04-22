package com.meritcap.DTOs.responseDTOs.lead;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LeadStatusCountDto {
    private Long totalLeads;
    private Long hotLeads;
    private Long immediateHotLeads;
    private Long warmLeads;
    private Long coldLeads;
    private Long featureLeads;
    private Long contactedLeads;
    private Long assignedLeads;
}

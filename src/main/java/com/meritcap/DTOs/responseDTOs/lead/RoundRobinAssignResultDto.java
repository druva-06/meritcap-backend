package com.meritcap.DTOs.responseDTOs.lead;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundRobinAssignResultDto {
    private int assignedCount;
    // counselor full name → number of leads assigned in this batch
    private Map<String, Integer> counselorAssignments;
}

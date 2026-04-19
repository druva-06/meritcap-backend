package com.meritcap.DTOs.requestDTOs.lead;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoundRobinAssignRequestDto {
    // If null or empty, all unassigned leads will be distributed
    private List<Long> leadIds;
}

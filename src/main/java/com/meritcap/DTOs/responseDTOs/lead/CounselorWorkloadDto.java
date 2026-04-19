package com.meritcap.DTOs.responseDTOs.lead;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselorWorkloadDto {
    private Long counselorId;
    private String name;
    private String email;
    private int totalLeads;
    private int hotLeads;
    private int immediateHotLeads;
    private int warmLeads;
    private int coldLeads;
    private int featureLeads;
    private int contactedLeads;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAssignedAt;
}

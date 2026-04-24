package com.meritcap.service;

import com.meritcap.DTOs.responseDTOs.lead.AssignmentHistoryDto;
import com.meritcap.DTOs.responseDTOs.lead.CounselorWorkloadDto;
import com.meritcap.DTOs.responseDTOs.lead.DirectAssignResultDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadResponseDto;
import com.meritcap.DTOs.responseDTOs.lead.RoundRobinAssignResultDto;
import com.meritcap.model.User;

import java.util.List;

public interface RoundRobinService {

    RoundRobinAssignResultDto assignLeadsRoundRobin(List<Long> leadIds, String adminEmail);

    DirectAssignResultDto directAssignLeads(String campaignName, Long counselorId, int count, String sortBy, String adminEmail);

    LeadResponseDto reassignLead(Long leadId, Long newCounselorId, String reason, String byEmail);

    List<CounselorWorkloadDto> getCounselorWorkload();

    List<AssignmentHistoryDto> getLeadAssignmentHistory(Long leadId);

    void autoAssignNewStudentLead(User user);
}

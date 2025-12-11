package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadFilterDto;
import com.consultancy.education.DTOs.requestDTOs.lead.LeadRequestDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadPageResponseDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadResponseDto;

public interface LeadService {
    LeadResponseDto createLead(LeadRequestDto leadRequestDto, String createdByUserEmail);
    
    LeadPageResponseDto getLeads(LeadFilterDto filterDto);
}

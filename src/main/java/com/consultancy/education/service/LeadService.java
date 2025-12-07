package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadRequestDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadResponseDto;

public interface LeadService {
    LeadResponseDto createLead(LeadRequestDto leadRequestDto, String createdByUserEmail);
}

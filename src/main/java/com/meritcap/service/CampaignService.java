package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.campaign.CampaignRequestDto;
import com.meritcap.DTOs.responseDTOs.campaign.CampaignStatsDto;

import java.util.List;

public interface CampaignService {

    CampaignStatsDto createCampaign(CampaignRequestDto dto, String userEmail);

    List<CampaignStatsDto> getAllCampaignsWithStats();
}

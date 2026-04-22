package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.campaign.CampaignRequestDto;
import com.meritcap.DTOs.responseDTOs.campaign.CampaignStatsDto;
import com.meritcap.exception.CustomException;
import com.meritcap.model.Campaign;
import com.meritcap.repository.CampaignRepository;
import com.meritcap.repository.LeadRepository;
import com.meritcap.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final LeadRepository leadRepository;

    @Override
    @Transactional
    public CampaignStatsDto createCampaign(CampaignRequestDto dto, String userEmail) {
        if (campaignRepository.existsByName(dto.getName())) {
            throw new CustomException("A campaign with this name already exists");
        }

        Campaign campaign = Campaign.builder()
                .name(dto.getName())
                .source(dto.getSource())
                .description(dto.getDescription())
                .qrCode(dto.getQrCode())
                .createdBy(userEmail)
                .build();

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created: {} by {}", saved.getName(), userEmail);

        return toDto(saved, 0L, 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignStatsDto> getAllCampaignsWithStats() {
        List<Campaign> campaigns = campaignRepository.findAllByOrderByCreatedAtDesc();

        if (campaigns.isEmpty()) {
            return List.of();
        }

        List<String> names = campaigns.stream().map(Campaign::getName).collect(Collectors.toList());
        List<Object[]> statsRows = leadRepository.getCampaignLeadStats(names);

        // Build a lookup map: campaignName -> [totalLeads, assignedLeads, duplicateLeads]
        Map<String, long[]> statsMap = new HashMap<>();
        for (Object[] row : statsRows) {
            String campaignName = (String) row[0];
            long total = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            long assigned = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long duplicates = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            statsMap.put(campaignName, new long[]{total, assigned, duplicates});
        }

        return campaigns.stream().map(c -> {
            long[] s = statsMap.getOrDefault(c.getName(), new long[]{0L, 0L, 0L});
            return toDto(c, s[0], s[1], s[2]);
        }).collect(Collectors.toList());
    }

    private CampaignStatsDto toDto(Campaign c, long total, long assigned, long duplicates) {
        return CampaignStatsDto.builder()
                .id(c.getId())
                .name(c.getName())
                .source(c.getSource())
                .description(c.getDescription())
                .qrCode(c.getQrCode())
                .status(c.getStatus())
                .createdBy(c.getCreatedBy())
                .createdDate(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .totalLeads(total)
                .assignedLeads(assigned)
                .unassignedLeads(total - assigned)
                .duplicateLeads(duplicates)
                .build();
    }
}

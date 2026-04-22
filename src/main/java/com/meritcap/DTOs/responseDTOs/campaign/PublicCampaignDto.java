package com.meritcap.DTOs.responseDTOs.campaign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicCampaignDto {
    Long id;
    String name;
    String source;
}

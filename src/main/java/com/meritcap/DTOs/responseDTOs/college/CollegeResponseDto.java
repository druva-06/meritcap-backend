package com.meritcap.DTOs.responseDTOs.college;

import com.meritcap.enums.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CollegeResponseDto {
    private Long id;
    private String slug;
    private String name;
    private String campusName;
    private String campusCode;
    private String country;
    private Long countryId;
    private Integer establishedYear;
    private String ranking;
    private String description;
    private String websiteUrl;
    private String collegeLogo;
    private String campusGalleryVideoLink;
    private String bannerUrl;
    private ActiveStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String faqsUniversity;
}

package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeCreateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.model.College;
import com.consultancy.education.model.Seo;

public class CollegeTransformer {

    public static College toEntity(CollegeCreateRequestDto req, Seo seo) {
        return College.builder()
                .slug(req.getSlug())
                .name(req.getName())
                .campusName(req.getCampusName())
                .campusCode(req.getCampusCode())
                .country(req.getCountry())
                .establishedYear(req.getEstablishedYear())
                .ranking(req.getRanking())
                .description(req.getDescription())
                .websiteUrl(req.getWebsiteUrl())
                .collegeLogo(req.getCollegeLogo())
                .campusGalleryVideoLink(req.getCampusGalleryVideoLink())
                .bannerUrl(req.getBannerUrl())
                .status(req.getStatus())
                .seo(seo)
                .build();
    }

    public static CollegeResponseDto toResDTO(College c) {
        return new CollegeResponseDto(
                c.getId(), c.getSlug(), c.getName(), c.getCampusName(), c.getCampusCode(),
                c.getCountry(), c.getEstablishedYear(), c.getRanking(), c.getDescription(),
                c.getWebsiteUrl(), c.getCollegeLogo(), c.getCampusGalleryVideoLink(), c.getBannerUrl(),
                c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(), c.getCreatedBy(), c.getUpdatedBy()
        );
    }
}

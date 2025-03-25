package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.model.College;
import com.consultancy.education.model.Seo;

import java.util.ArrayList;
import java.util.List;

public class CollegeTransformer {

    public static College toEntity(CollegeRequestDto collegeRequestDto){
        return College.builder()
                .name(collegeRequestDto.getName())
                .campusCode(collegeRequestDto.getCampusCode())
                .campus(collegeRequestDto.getCampus())
                .websiteUrl(collegeRequestDto.getWebsiteUrl())
                .country(collegeRequestDto.getCountry())
                .collegeLogo(collegeRequestDto.getCollegeLogo())
                .establishedYear(collegeRequestDto.getEstablishedYear())
                .ranking(collegeRequestDto.getRanking())
                .description(collegeRequestDto.getDescription())
                .campusGalleryVideoLink(collegeRequestDto.getCampusGalleryVideoLink())
                .build();
    }

    public static CollegeRequestDto toReqDTO(College college){
        return CollegeRequestDto.builder()
                .name(college.getName())
                .campus(college.getCampus())
                .campusCode(college.getCampusCode())
                .websiteUrl(college.getWebsiteUrl())
                .collegeLogo(college.getCollegeLogo())
                .establishedYear(college.getEstablishedYear())
                .ranking(college.getRanking())
                .country(college.getCountry())
                .description(college.getDescription())
                .campusGalleryVideoLink(college.getCampusGalleryVideoLink())
                .build();
    }

    public static CollegeResponseDto toResDTO(College college){
        return CollegeResponseDto.builder()
                .collegeId(college.getId())
                .collegeName(college.getName())
                .collegeName(college.getCampus())
                .campusCode(college.getCampusCode())
                .country(college.getCountry())
                .collegeLogo(college.getCollegeLogo())
                .websiteUrl(college.getWebsiteUrl())
                .campusGalleryVideoLink(college.getCampusGalleryVideoLink())
                .establishedYear(college.getEstablishedYear())
                .ranking(college.getRanking())
                .description(college.getDescription())
                .build();
    }

    public static List<CollegeResponseDto> toResDTO(List<College> colleges){
        List<CollegeResponseDto> collegeResponseDtoList = new ArrayList<>();
        for (College college : colleges) {
            collegeResponseDtoList.add(CollegeTransformer.toResDTO(college));
        }
        return collegeResponseDtoList;
    }

    public static void updateCollegeDetails(College existingCollege, CollegeRequestDto collegeRequestDto) {
        existingCollege.setName(collegeRequestDto.getName());
        existingCollege.setCampus(collegeRequestDto.getCampus());
        existingCollege.setCampusCode(collegeRequestDto.getCampusCode());
        existingCollege.setWebsiteUrl(collegeRequestDto.getWebsiteUrl());
        existingCollege.setCountry(collegeRequestDto.getCountry());
        existingCollege.setCollegeLogo(collegeRequestDto.getCollegeLogo());
        existingCollege.setEstablishedYear(collegeRequestDto.getEstablishedYear());
        existingCollege.setRanking(collegeRequestDto.getRanking());
        existingCollege.setDescription(collegeRequestDto.getDescription());
        existingCollege.setCampusGalleryVideoLink(collegeRequestDto.getCampusGalleryVideoLink());
    }

    public static void updateCollegeDetailsEntityToEntity(College existingCollege, College college) {
        existingCollege.setWebsiteUrl(college.getWebsiteUrl());
        existingCollege.setCollegeLogo(college.getCollegeLogo());
        existingCollege.setEstablishedYear(college.getEstablishedYear());
        existingCollege.setRanking(college.getRanking());
        existingCollege.setDescription(college.getDescription());
        existingCollege.setCampusGalleryVideoLink(college.getCampusGalleryVideoLink());
    }


    public static College reqDtoToReqDto(College college, String campus) {
        return College.builder()
                .name(college.getName())
                .campus(campus)
                .websiteUrl(college.getWebsiteUrl())
                .collegeLogo(college.getCollegeLogo())
                .country(college.getCountry())
                .establishedYear(college.getEstablishedYear())
                .ranking(college.getRanking())
                .description(college.getDescription())
                .campusGalleryVideoLink(college.getCampusGalleryVideoLink())
                .build();
    }
}

package com.consultancy.education.transformer;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadRequestDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadListResponseDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadResponseDto;
import com.consultancy.education.enums.LeadStatus;
import com.consultancy.education.model.Lead;
import com.consultancy.education.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeadTransformer {

    public static Lead toEntity(LeadRequestDto dto, User createdBy, User assignedTo) {
        Lead lead = Lead.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .country(dto.getCountry())
                .status(dto.getStatus() != null ? dto.getStatus() : LeadStatus.WARM)
                .score(dto.getScore() != null ? dto.getScore() : 0)
                .leadSource(dto.getLeadSource())
                .preferredCountries(dto.getPreferredCountries())
                .preferredCourses(dto.getPreferredCourses())
                .budgetRange(dto.getBudgetRange())
                .intake(dto.getIntake())
                .tags(dto.getTags() != null ? String.join(",", dto.getTags()) : null)
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .encryptedPersonalDetails(dto.getEncryptedPersonalDetails())
                .encryptedAcademicDetails(dto.getEncryptedAcademicDetails())
                .encryptedPreferences(dto.getEncryptedPreferences())
                .isDuplicate(false)
                .build();

        return lead;
    }

    public static LeadResponseDto toResponseDto(Lead lead) {
        LeadResponseDto dto = LeadResponseDto.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .country(lead.getCountry())
                .status(lead.getStatus())
                .score(lead.getScore())
                .leadSource(lead.getLeadSource())
                .preferredCountries(lead.getPreferredCountries())
                .preferredCourses(lead.getPreferredCourses())
                .budgetRange(lead.getBudgetRange())
                .intake(lead.getIntake())
                .tags(lead.getTags() != null ? Arrays.asList(lead.getTags().split(",")) : null)
                .encryptedPersonalDetails(lead.getEncryptedPersonalDetails())
                .encryptedAcademicDetails(lead.getEncryptedAcademicDetails())
                .encryptedPreferences(lead.getEncryptedPreferences())
                .isDuplicate(lead.getIsDuplicate())
                .duplicateOf(lead.getDuplicateOf())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();

        if (lead.getAssignedTo() != null) {
            dto.setAssignedToId(lead.getAssignedTo().getId());
            dto.setAssignedToName(lead.getAssignedTo().getFirstName() + " " + lead.getAssignedTo().getLastName());
        }

        if (lead.getCreatedBy() != null) {
            dto.setCreatedById(lead.getCreatedBy().getId());
            dto.setCreatedByName(lead.getCreatedBy().getFirstName() + " " + lead.getCreatedBy().getLastName());
        }

        return dto;
    }

    public static LeadListResponseDto toListResponseDto(Lead lead) {
        LeadListResponseDto dto = LeadListResponseDto.builder()
                .id(lead.getId())
                .name(lead.getFirstName() + " " + lead.getLastName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .status(lead.getStatus())
                .score(lead.getScore())
                .tags(lead.getTags() != null ? Arrays.asList(lead.getTags().split(",")) : null)
                .isDuplicate(lead.getIsDuplicate())
                .createdAt(lead.getCreatedAt())
                .build();

        if (lead.getAssignedTo() != null) {
            dto.setAssignedToName(lead.getAssignedTo().getFirstName() + " " + lead.getAssignedTo().getLastName());
        }

        return dto;
    }

    public static List<LeadListResponseDto> toListResponseDtos(List<Lead> leads) {
        return leads.stream()
                .map(LeadTransformer::toListResponseDto)
                .collect(Collectors.toList());
    }

    public static void updateEntity(Lead lead, LeadRequestDto dto, User assignedTo) {
        if (dto.getFirstName() != null) {
            lead.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            lead.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            lead.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            lead.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getCountry() != null) {
            lead.setCountry(dto.getCountry());
        }
        if (dto.getStatus() != null) {
            lead.setStatus(dto.getStatus());
        }
        if (dto.getScore() != null) {
            lead.setScore(dto.getScore());
        }
        if (dto.getLeadSource() != null) {
            lead.setLeadSource(dto.getLeadSource());
        }
        if (dto.getPreferredCountries() != null) {
            lead.setPreferredCountries(dto.getPreferredCountries());
        }
        if (dto.getPreferredCourses() != null) {
            lead.setPreferredCourses(dto.getPreferredCourses());
        }
        if (dto.getBudgetRange() != null) {
            lead.setBudgetRange(dto.getBudgetRange());
        }
        if (dto.getIntake() != null) {
            lead.setIntake(dto.getIntake());
        }
        if (dto.getTags() != null) {
            lead.setTags(String.join(",", dto.getTags()));
        }
        if (assignedTo != null) {
            lead.setAssignedTo(assignedTo);
        }
        if (dto.getEncryptedPersonalDetails() != null) {
            lead.setEncryptedPersonalDetails(dto.getEncryptedPersonalDetails());
        }
        if (dto.getEncryptedAcademicDetails() != null) {
            lead.setEncryptedAcademicDetails(dto.getEncryptedAcademicDetails());
        }
        if (dto.getEncryptedPreferences() != null) {
            lead.setEncryptedPreferences(dto.getEncryptedPreferences());
        }
    }
}

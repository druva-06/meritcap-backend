package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.lead.LeadFilterDto;
import com.meritcap.DTOs.requestDTOs.lead.LeadRequestDto;
import com.meritcap.DTOs.requestDTOs.lead.UpdateLeadRequestDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadPageResponseDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadResponseDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadStatusCountDto;
import com.meritcap.enums.LeadStatus;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Lead;
import com.meritcap.model.User;
import com.meritcap.repository.LeadRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.repository.specification.LeadSpecification;
import com.meritcap.service.LeadService;
import com.meritcap.transformer.LeadTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public LeadServiceImpl(LeadRepository leadRepository, UserRepository userRepository) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public LeadResponseDto createLead(LeadRequestDto leadRequestDto, String createdByUserEmail) {
        log.info("Creating lead for email: {}", leadRequestDto.getEmail());

        // Validate creator user exists by email
        User createdBy = userRepository.findByEmail(createdByUserEmail);
        if (createdBy == null) {
            log.error("Creator user not found with email: {}", createdByUserEmail);
            throw new NotFoundException("User not found");
        }

        // Check for duplicates
        List<Lead> existingLeads = leadRepository.findByEmailOrPhoneNumber(
                leadRequestDto.getEmail(),
                leadRequestDto.getPhoneNumber());

        if (!existingLeads.isEmpty()) {
            log.warn("Duplicate lead found for email: {} or phone: {}",
                    leadRequestDto.getEmail(), leadRequestDto.getPhoneNumber());

            Lead existingLead = existingLeads.get(0);

            // Throw exception for duplicate lead
            throw new CustomException(
                    String.format("Duplicate lead found. Lead already exists with ID: %d", existingLead.getId()));
        }

        // Validate and get assigned user if provided
        User assignedTo = null;
        if (leadRequestDto.getAssignedTo() != null) {
            assignedTo = userRepository.findById(leadRequestDto.getAssignedTo())
                    .orElseThrow(() -> {
                        log.error("Assigned user not found with ID: {}", leadRequestDto.getAssignedTo());
                        return new NotFoundException("Assigned counselor not found");
                    });
            log.info("Lead will be assigned to: {} {}", assignedTo.getFirstName(), assignedTo.getLastName());
        }

        // Create lead entity
        Lead lead = LeadTransformer.toEntity(leadRequestDto, createdBy, assignedTo);

        // Save lead
        Lead savedLead = leadRepository.save(lead);
        log.info("Lead created successfully with ID: {}", savedLead.getId());

        // Convert to response DTO
        return LeadTransformer.toResponseDto(savedLead);
    }

    @Override
    public LeadPageResponseDto getLeads(LeadFilterDto filterDto) {
        log.info("Fetching leads with filters: {}", filterDto);

        // Build specification from filter
        Specification<Lead> spec = LeadSpecification.filterLeads(filterDto);

        // Build pageable with sorting
        Sort sort = Sort.by(
                filterDto.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                filterDto.getSortBy());

        Pageable pageable = PageRequest.of(
                filterDto.getPage(),
                filterDto.getSize(),
                sort);

        // Fetch leads with specification and pagination
        Page<Lead> leadPage = leadRepository.findAll(spec, pageable);

        log.info("Found {} leads (Page {}/{})",
                leadPage.getTotalElements(),
                leadPage.getNumber() + 1,
                leadPage.getTotalPages());

        // Convert to response DTO
        return LeadTransformer.toPageResponse(leadPage);
    }

    @Override
    public LeadResponseDto getLeadById(Long leadId) {
        log.info("Fetching lead with ID: {}", leadId);

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> {
                    log.error("Lead not found with ID: {}", leadId);
                    return new NotFoundException("Lead not found with ID: " + leadId);
                });

        log.info("Lead found: {} {}", lead.getFirstName(), lead.getLastName());
        return LeadTransformer.toResponseDto(lead);
    }

    @Override
    @Transactional
    public LeadResponseDto updateLead(Long leadId, UpdateLeadRequestDto updateLeadRequestDto,
            String updatedByUserEmail) {
        log.info("Updating lead with ID: {}", leadId);

        // Find existing lead
        Lead existingLead = leadRepository.findById(leadId)
                .orElseThrow(() -> {
                    log.error("Lead not found with ID: {}", leadId);
                    return new NotFoundException("Lead not found with ID: " + leadId);
                });

        // Validate updater user exists by email
        User updatedBy = userRepository.findByEmail(updatedByUserEmail);
        if (updatedBy == null) {
            log.error("Updater user not found with email: {}", updatedByUserEmail);
            throw new NotFoundException("User not found");
        }

        // Update editable fields (excluding email and phone)
        existingLead.setFirstName(updateLeadRequestDto.getFirstName());
        existingLead.setLastName(updateLeadRequestDto.getLastName());
        existingLead.setCountry(updateLeadRequestDto.getCountry());
        existingLead.setStatus(updateLeadRequestDto.getStatus());
        existingLead.setScore(updateLeadRequestDto.getScore());
        existingLead.setLeadSource(updateLeadRequestDto.getLeadSource());
        existingLead.setCampaign(updateLeadRequestDto.getCampaign());
        existingLead.setPreferredCountries(updateLeadRequestDto.getPreferredCountries());
        existingLead.setPreferredCourses(updateLeadRequestDto.getPreferredCourses());
        existingLead.setBudgetRange(updateLeadRequestDto.getBudgetRange());
        existingLead.setIntake(updateLeadRequestDto.getIntake());
        existingLead.setTags(
                updateLeadRequestDto.getTags() != null ? String.join(",", updateLeadRequestDto.getTags()) : null);
        existingLead.setEncryptedPersonalDetails(updateLeadRequestDto.getEncryptedPersonalDetails());
        existingLead.setEncryptedAcademicDetails(updateLeadRequestDto.getEncryptedAcademicDetails());
        existingLead.setEncryptedPreferences(updateLeadRequestDto.getEncryptedPreferences());

        // Update assigned user if provided
        if (updateLeadRequestDto.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(updateLeadRequestDto.getAssignedTo())
                    .orElseThrow(() -> {
                        log.error("Assigned user not found with ID: {}", updateLeadRequestDto.getAssignedTo());
                        return new NotFoundException("Assigned counselor not found");
                    });
            existingLead.setAssignedTo(assignedTo);
            log.info("Lead reassigned to: {} {}", assignedTo.getFirstName(), assignedTo.getLastName());
        }

        // Save updated lead
        Lead updatedLead = leadRepository.save(existingLead);
        log.info("Lead updated successfully with ID: {}", updatedLead.getId());

        // Convert to response DTO
        return LeadTransformer.toResponseDto(updatedLead);
    }

    @Override
    public Long countTotalLeads() {
        log.info("Counting total leads");
        Long count = leadRepository.count();
        log.info("Total leads count: {}", count);
        return count;
    }

    @Override
    public LeadStatusCountDto getLeadStatusCounts() {
        log.info("Fetching lead status counts");

        Long totalLeads = leadRepository.count();
        Long hotLeads = leadRepository.countByStatus(LeadStatus.HOT);
        Long immediateHotLeads = leadRepository.countByStatus(LeadStatus.IMMEDIATE_HOT);
        Long warmLeads = leadRepository.countByStatus(LeadStatus.WARM);
        Long coldLeads = leadRepository.countByStatus(LeadStatus.COLD);
        Long featureLeads = leadRepository.countByStatus(LeadStatus.FEATURE_LEAD);
        Long contactedLeads = leadRepository.countByStatus(LeadStatus.CONTACTED);
        Long assignedLeads = leadRepository.countByAssignedToIsNotNull();

        log.info(
                "Status counts - Total: {}, HOT: {}, IMMEDIATE_HOT: {}, WARM: {}, COLD: {}, FEATURE_LEAD: {}, CONTACTED: {}, ASSIGNED: {}",
                totalLeads, hotLeads, immediateHotLeads, warmLeads, coldLeads, featureLeads, contactedLeads, assignedLeads);

        return LeadStatusCountDto.builder()
                .totalLeads(totalLeads)
                .hotLeads(hotLeads)
                .immediateHotLeads(immediateHotLeads)
                .warmLeads(warmLeads)
                .coldLeads(coldLeads)
                .featureLeads(featureLeads)
                .contactedLeads(contactedLeads)
                .assignedLeads(assignedLeads)
                .build();
    }
}

package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadFilterDto;
import com.consultancy.education.DTOs.requestDTOs.lead.LeadRequestDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadPageResponseDto;
import com.consultancy.education.DTOs.responseDTOs.lead.LeadResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.Lead;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.LeadRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.repository.specification.LeadSpecification;
import com.consultancy.education.service.LeadService;
import com.consultancy.education.transformer.LeadTransformer;
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
            filterDto.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            filterDto.getPage(), 
            filterDto.getSize(), 
            sort
        );

        // Fetch leads with specification and pagination
        Page<Lead> leadPage = leadRepository.findAll(spec, pageable);

        log.info("Found {} leads (Page {}/{})", 
                leadPage.getTotalElements(), 
                leadPage.getNumber() + 1, 
                leadPage.getTotalPages());

        // Convert to response DTO
        return LeadTransformer.toPageResponse(leadPage);
    }
}

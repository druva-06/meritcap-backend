// src/main/java/com/consultancy/education/service/impl/CollegeServiceImpl.java
package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeCreateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.College;
import com.consultancy.education.model.Seo;
import com.consultancy.education.repository.CollegeRepository;
import com.consultancy.education.repository.SeoRepository;
import com.consultancy.education.service.CollegeService;
import com.consultancy.education.transformer.CollegeTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepo;
    private final SeoRepository seoRepo;

    public CollegeServiceImpl(CollegeRepository collegeRepo, SeoRepository seoRepo) {
        this.collegeRepo = collegeRepo;
        this.seoRepo = seoRepo;
    }

    @Override
    public CollegeResponseDto create(CollegeCreateRequestDto req) {
        log.info("CreateCollege service started: slug={}, campusCode={}", req.getSlug(), req.getCampusCode());

        if (collegeRepo.existsByCampusCode(req.getCampusCode())) {
            log.error("CreateCollege conflict: campusCode exists {}", req.getCampusCode());
            throw new CustomException("campusCode already exists.");
        }
        if (collegeRepo.existsBySlug(req.getSlug())) {
            log.error("CreateCollege conflict: slug exists {}", req.getSlug());
            throw new CustomException("slug already exists.");
        }

        Seo seo = null;
        if (req.getSeoId() != null) {
            seo = seoRepo.findById(req.getSeoId())
                    .orElseThrow(() -> {
                        log.error("CreateCollege: seoId not found {}", req.getSeoId());
                        return new CustomException("seoId not found.");
                    });
        }

        try {
            College saved = collegeRepo.save(CollegeTransformer.toEntity(req, seo));
            log.info("CreateCollege persisted: id={}, slug={}, campusCode={}", saved.getId(), saved.getSlug(), saved.getCampusCode());
            return CollegeTransformer.toResDTO(saved);
        } catch (DataIntegrityViolationException ex) {
            log.error("CreateCollege unique constraint violation for slug={} campusCode={}", req.getSlug(), req.getCampusCode(), ex);
            throw new CustomException("Duplicate slug or campusCode.");
        } catch (Exception ex) {
            log.error("CreateCollege unexpected error: {}", ex.getMessage(), ex);
            throw new CustomException("Unexpected error while creating college.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeResponseDto getById(Long id) {
        log.info("GetCollegeById service started: id={}", id);
        College college = collegeRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("GetCollegeById not found: id={}", id);
                    return new NotFoundException("College not found.");
                });
        CollegeResponseDto resp = CollegeTransformer.toResDTO(college);
        log.info("GetCollegeById success: id={}, slug={}", resp.getId(), resp.getSlug());
        return resp;
    }
}


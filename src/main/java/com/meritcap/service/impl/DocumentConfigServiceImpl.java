package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.documentconfig.*;
import com.meritcap.DTOs.responseDTOs.documentconfig.*;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.*;
import com.meritcap.repository.*;
import com.meritcap.service.DocumentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentConfigServiceImpl implements DocumentConfigService {

    private final DocumentTypeEntityRepository documentTypeEntityRepository;
    private final ProfileDocumentRequirementRepository profileDocumentRequirementRepository;
    private final CountryDocumentRequirementRepository countryDocumentRequirementRepository;
    private final CountryRepository countryRepository;
    private final DocumentRepository documentRepository;

    // =====================================================================
    // Document Types
    // =====================================================================

    @Override
    public List<DocumentTypeResponseDto> getAllDocumentTypes() {
        return documentTypeEntityRepository.findAll()
                .stream().map(this::toDocumentTypeDto).collect(Collectors.toList());
    }

    @Override
    public DocumentTypeResponseDto getDocumentTypeById(Long id) {
        return toDocumentTypeDto(findDocumentTypeById(id));
    }

    @Override
    public DocumentTypeResponseDto createDocumentType(DocumentTypeRequestDto requestDto) {
        if (documentTypeEntityRepository.existsByCodeIgnoreCase(requestDto.getCode())) {
            throw new AlreadyExistException(Collections.singletonList(
                    "Document type with code '" + requestDto.getCode() + "' already exists"));
        }

        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .name(requestDto.getName())
                .code(requestDto.getCode())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .allowMultiple(requestDto.getAllowMultiple() != null ? requestDto.getAllowMultiple() : false)
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        entity = documentTypeEntityRepository.save(entity);
        log.info("Created document type id={}, code={}", entity.getId(), entity.getCode());
        return toDocumentTypeDto(entity);
    }

    @Override
    public DocumentTypeResponseDto updateDocumentType(Long id, DocumentTypeRequestDto requestDto) {
        DocumentTypeEntity entity = findDocumentTypeById(id);

        if (!entity.getCode().equalsIgnoreCase(requestDto.getCode())
                && documentTypeEntityRepository.existsByCodeIgnoreCase(requestDto.getCode())) {
            throw new AlreadyExistException(Collections.singletonList(
                    "Document type with code '" + requestDto.getCode() + "' already exists"));
        }

        entity.setName(requestDto.getName());
        entity.setCode(requestDto.getCode());
        entity.setDescription(requestDto.getDescription());
        if (requestDto.getCategory() != null) entity.setCategory(requestDto.getCategory());
        if (requestDto.getAllowMultiple() != null) entity.setAllowMultiple(requestDto.getAllowMultiple());
        if (requestDto.getIsActive() != null) entity.setIsActive(requestDto.getIsActive());

        entity = documentTypeEntityRepository.save(entity);
        log.info("Updated document type id={}", entity.getId());
        return toDocumentTypeDto(entity);
    }

    @Override
    public void deleteDocumentType(Long id) {
        DocumentTypeEntity entity = findDocumentTypeById(id);
        documentTypeEntityRepository.delete(entity);
        log.info("Soft-deleted document type id={}", id);
    }

    // =====================================================================
    // Profile Document Requirements
    // =====================================================================

    @Override
    public List<ProfileDocumentRequirementResponseDto> getProfileRequirements() {
        return profileDocumentRequirementRepository.findAllByOrderByDisplayOrderAsc()
                .stream().map(this::toProfileReqDto).collect(Collectors.toList());
    }

    @Override
    public ProfileDocumentRequirementResponseDto createProfileRequirement(ProfileDocumentRequirementRequestDto requestDto) {
        if (profileDocumentRequirementRepository.existsByDocumentTypeId(requestDto.getDocumentTypeId())) {
            throw new AlreadyExistException(Collections.singletonList(
                    "Profile requirement for this document type already exists"));
        }
        DocumentTypeEntity docType = findDocumentTypeById(requestDto.getDocumentTypeId());

        ProfileDocumentRequirement req = ProfileDocumentRequirement.builder()
                .documentType(docType)
                .isRequired(requestDto.getIsRequired() != null ? requestDto.getIsRequired() : true)
                .minCount(requestDto.getMinCount() != null ? requestDto.getMinCount() : 1)
                .displayOrder(requestDto.getDisplayOrder() != null ? requestDto.getDisplayOrder() : 0)
                .build();

        req = profileDocumentRequirementRepository.save(req);
        log.info("Created profile document requirement id={}", req.getId());
        return toProfileReqDto(req);
    }

    @Override
    public ProfileDocumentRequirementResponseDto updateProfileRequirement(Long id, ProfileDocumentRequirementRequestDto requestDto) {
        ProfileDocumentRequirement req = profileDocumentRequirementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile document requirement not found: " + id));

        if (requestDto.getDocumentTypeId() != null
                && !req.getDocumentType().getId().equals(requestDto.getDocumentTypeId())) {
            if (profileDocumentRequirementRepository.existsByDocumentTypeId(requestDto.getDocumentTypeId())) {
                throw new AlreadyExistException(Collections.singletonList(
                        "Profile requirement for this document type already exists"));
            }
            req.setDocumentType(findDocumentTypeById(requestDto.getDocumentTypeId()));
        }

        if (requestDto.getIsRequired() != null) req.setIsRequired(requestDto.getIsRequired());
        if (requestDto.getMinCount() != null) req.setMinCount(requestDto.getMinCount());
        if (requestDto.getDisplayOrder() != null) req.setDisplayOrder(requestDto.getDisplayOrder());

        req = profileDocumentRequirementRepository.save(req);
        return toProfileReqDto(req);
    }

    @Override
    public void deleteProfileRequirement(Long id) {
        ProfileDocumentRequirement req = profileDocumentRequirementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile document requirement not found: " + id));
        profileDocumentRequirementRepository.delete(req);
        log.info("Soft-deleted profile document requirement id={}", id);
    }

    // =====================================================================
    // Country Document Requirements
    // =====================================================================

    @Override
    public List<CountryDocumentRequirementResponseDto> getCountryRequirements(Long countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new NotFoundException("Country not found: " + countryId);
        }
        return countryDocumentRequirementRepository.findByCountryIdOrderByDisplayOrderAsc(countryId)
                .stream().map(this::toCountryReqDto).collect(Collectors.toList());
    }

    @Override
    public CountryDocumentRequirementResponseDto createCountryRequirement(CountryDocumentRequirementRequestDto requestDto) {
        if (countryDocumentRequirementRepository.existsByCountryIdAndDocumentTypeId(
                requestDto.getCountryId(), requestDto.getDocumentTypeId())) {
            throw new AlreadyExistException(Collections.singletonList(
                    "Requirement for this country and document type already exists"));
        }

        Country country = countryRepository.findById(requestDto.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + requestDto.getCountryId()));
        DocumentTypeEntity docType = findDocumentTypeById(requestDto.getDocumentTypeId());

        CountryDocumentRequirement req = CountryDocumentRequirement.builder()
                .country(country)
                .documentType(docType)
                .isRequired(requestDto.getIsRequired() != null ? requestDto.getIsRequired() : true)
                .minCount(requestDto.getMinCount() != null ? requestDto.getMinCount() : 1)
                .displayOrder(requestDto.getDisplayOrder() != null ? requestDto.getDisplayOrder() : 0)
                .build();

        req = countryDocumentRequirementRepository.save(req);
        log.info("Created country doc requirement id={} for countryId={}", req.getId(), requestDto.getCountryId());
        return toCountryReqDto(req);
    }

    @Override
    public CountryDocumentRequirementResponseDto updateCountryRequirement(Long id, CountryDocumentRequirementRequestDto requestDto) {
        CountryDocumentRequirement req = countryDocumentRequirementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Country document requirement not found: " + id));

        if (requestDto.getIsRequired() != null) req.setIsRequired(requestDto.getIsRequired());
        if (requestDto.getMinCount() != null) req.setMinCount(requestDto.getMinCount());
        if (requestDto.getDisplayOrder() != null) req.setDisplayOrder(requestDto.getDisplayOrder());

        req = countryDocumentRequirementRepository.save(req);
        return toCountryReqDto(req);
    }

    @Override
    public void deleteCountryRequirement(Long id) {
        CountryDocumentRequirement req = countryDocumentRequirementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Country document requirement not found: " + id));
        countryDocumentRequirementRepository.delete(req);
        log.info("Soft-deleted country doc requirement id={}", id);
    }

    @Override
    @Transactional
    public List<CountryDocumentRequirementResponseDto> bulkSaveCountryRequirements(
            CountryDocumentRequirementBulkRequestDto requestDto) {

        Country country = countryRepository.findById(requestDto.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + requestDto.getCountryId()));

        // Soft-delete all existing requirements for this country
        countryDocumentRequirementRepository.softDeleteAllByCountryId(requestDto.getCountryId());

        List<CountryDocumentRequirement> saved = new ArrayList<>();
        for (CountryDocumentRequirementBulkRequestDto.RequirementItem item : requestDto.getRequirements()) {
            DocumentTypeEntity docType = findDocumentTypeById(item.getDocumentTypeId());

            CountryDocumentRequirement req = CountryDocumentRequirement.builder()
                    .country(country)
                    .documentType(docType)
                    .isRequired(item.getIsRequired() != null ? item.getIsRequired() : true)
                    .minCount(item.getMinCount() != null ? item.getMinCount() : 1)
                    .displayOrder(item.getDisplayOrder() != null ? item.getDisplayOrder() : 0)
                    .build();

            saved.add(countryDocumentRequirementRepository.save(req));
        }

        log.info("Bulk saved {} requirements for countryId={}", saved.size(), requestDto.getCountryId());
        return saved.stream().map(this::toCountryReqDto).collect(Collectors.toList());
    }

    // =====================================================================
    // Compliance Check
    // =====================================================================

    /**
     * Checks whether a student has uploaded all required documents for the
     * given country. The matching is done by comparing
     * {@code document_types.code} with {@code documents.document_type}.
     */
    @Override
    public DocumentComplianceResponseDto checkCountryDocumentCompliance(Long studentId, Long countryId) {
        List<CountryDocumentRequirement> requirements =
                countryDocumentRequirementRepository.findByCountryIdOrderByDisplayOrderAsc(countryId)
                        .stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsRequired()))
                        .collect(Collectors.toList());

        if (requirements.isEmpty()) {
            return DocumentComplianceResponseDto.builder()
                    .compliant(true)
                    .missingDocuments(Collections.emptyList())
                    .build();
        }

        // Fetch all non-deleted documents for this student
        List<Document> uploadedDocs = documentRepository
                .findAllByReferenceTypeAndReferenceIdAndIsDeletedFalse("STUDENT", studentId);

        // Count uploaded docs by document_type code
        Map<String, Long> uploadedCountByCode = uploadedDocs.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getDocumentType().toUpperCase(),
                        Collectors.counting()
                ));

        List<DocumentComplianceResponseDto.MissingDocument> missing = new ArrayList<>();
        for (CountryDocumentRequirement req : requirements) {
            String code = req.getDocumentType().getCode();
            long uploaded = uploadedCountByCode.getOrDefault(code, 0L);
            if (uploaded < req.getMinCount()) {
                missing.add(DocumentComplianceResponseDto.MissingDocument.builder()
                        .documentTypeName(req.getDocumentType().getName())
                        .documentTypeCode(code)
                        .required(req.getMinCount())
                        .uploaded((int) uploaded)
                        .isRequired(req.getIsRequired())
                        .build());
            }
        }

        boolean compliant = missing.isEmpty();
        log.info("Compliance check for studentId={}, countryId={}: compliant={}, missing={}",
                studentId, countryId, compliant, missing.size());

        return DocumentComplianceResponseDto.builder()
                .compliant(compliant)
                .missingDocuments(missing)
                .build();
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private DocumentTypeEntity findDocumentTypeById(Long id) {
        return documentTypeEntityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document type not found: " + id));
    }

    private DocumentTypeResponseDto toDocumentTypeDto(DocumentTypeEntity e) {
        return DocumentTypeResponseDto.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .category(e.getCategory())
                .allowMultiple(e.getAllowMultiple())
                .isActive(e.getIsActive())
                .build();
    }

    private ProfileDocumentRequirementResponseDto toProfileReqDto(ProfileDocumentRequirement r) {
        return ProfileDocumentRequirementResponseDto.builder()
                .id(r.getId())
                .documentTypeId(r.getDocumentType().getId())
                .documentTypeName(r.getDocumentType().getName())
                .documentTypeCode(r.getDocumentType().getCode())
                .allowMultiple(r.getDocumentType().getAllowMultiple())
                .isRequired(r.getIsRequired())
                .minCount(r.getMinCount())
                .displayOrder(r.getDisplayOrder())
                .build();
    }

    private CountryDocumentRequirementResponseDto toCountryReqDto(CountryDocumentRequirement r) {
        return CountryDocumentRequirementResponseDto.builder()
                .id(r.getId())
                .countryId(r.getCountry().getId())
                .countryName(r.getCountry().getName())
                .documentTypeId(r.getDocumentType().getId())
                .documentTypeName(r.getDocumentType().getName())
                .documentTypeCode(r.getDocumentType().getCode())
                .allowMultiple(r.getDocumentType().getAllowMultiple())
                .isRequired(r.getIsRequired())
                .minCount(r.getMinCount())
                .displayOrder(r.getDisplayOrder())
                .build();
    }
}

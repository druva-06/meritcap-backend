package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.documentconfig.*;
import com.meritcap.DTOs.responseDTOs.documentconfig.*;

import java.util.List;

public interface DocumentConfigService {

    // Document Types
    List<DocumentTypeResponseDto> getAllDocumentTypes();
    DocumentTypeResponseDto getDocumentTypeById(Long id);
    DocumentTypeResponseDto createDocumentType(DocumentTypeRequestDto requestDto);
    DocumentTypeResponseDto updateDocumentType(Long id, DocumentTypeRequestDto requestDto);
    void deleteDocumentType(Long id);

    // Profile Document Requirements
    List<ProfileDocumentRequirementResponseDto> getProfileRequirements();
    ProfileDocumentRequirementResponseDto createProfileRequirement(ProfileDocumentRequirementRequestDto requestDto);
    ProfileDocumentRequirementResponseDto updateProfileRequirement(Long id, ProfileDocumentRequirementRequestDto requestDto);
    void deleteProfileRequirement(Long id);

    // Country Document Requirements
    List<CountryDocumentRequirementResponseDto> getCountryRequirements(Long countryId);
    CountryDocumentRequirementResponseDto createCountryRequirement(CountryDocumentRequirementRequestDto requestDto);
    CountryDocumentRequirementResponseDto updateCountryRequirement(Long id, CountryDocumentRequirementRequestDto requestDto);
    void deleteCountryRequirement(Long id);
    List<CountryDocumentRequirementResponseDto> bulkSaveCountryRequirements(CountryDocumentRequirementBulkRequestDto requestDto);

    // Compliance
    DocumentComplianceResponseDto checkCountryDocumentCompliance(Long studentId, Long countryId);
}

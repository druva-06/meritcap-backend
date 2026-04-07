package com.meritcap.DTOs.responseDTOs.documentconfig;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountryDocumentRequirementResponseDto {
    Long id;
    Long countryId;
    String countryName;
    Long documentTypeId;
    String documentTypeName;
    String documentTypeCode;
    Boolean allowMultiple;
    Boolean isRequired;
    Integer minCount;
    Integer displayOrder;
}

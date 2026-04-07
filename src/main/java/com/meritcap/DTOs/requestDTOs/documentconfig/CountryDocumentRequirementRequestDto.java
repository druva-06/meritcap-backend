package com.meritcap.DTOs.requestDTOs.documentconfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountryDocumentRequirementRequestDto {

    @NotNull(message = "countryId is required")
    Long countryId;

    @NotNull(message = "documentTypeId is required")
    Long documentTypeId;

    Boolean isRequired;

    @Min(value = 1, message = "minCount must be at least 1")
    Integer minCount;

    Integer displayOrder;
}

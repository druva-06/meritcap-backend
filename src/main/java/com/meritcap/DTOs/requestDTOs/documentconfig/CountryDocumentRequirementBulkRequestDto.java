package com.meritcap.DTOs.requestDTOs.documentconfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountryDocumentRequirementBulkRequestDto {

    @NotNull(message = "countryId is required")
    Long countryId;

    @NotNull(message = "requirements list is required")
    List<RequirementItem> requirements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequirementItem {
        @NotNull
        Long documentTypeId;
        Boolean isRequired;
        @Min(1)
        Integer minCount;
        Integer displayOrder;
    }
}

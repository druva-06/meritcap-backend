package com.meritcap.DTOs.responseDTOs.documentconfig;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentComplianceResponseDto {

    Boolean compliant;
    List<MissingDocument> missingDocuments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissingDocument {
        String documentTypeName;
        String documentTypeCode;
        int required;
        int uploaded;
        Boolean isRequired;
    }
}

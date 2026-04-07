package com.meritcap.DTOs.responseDTOs.documentconfig;

import com.meritcap.enums.DocumentCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentTypeResponseDto {
    Long id;
    String name;
    String code;
    String description;
    DocumentCategory category;
    Boolean allowMultiple;
    Boolean isActive;
}

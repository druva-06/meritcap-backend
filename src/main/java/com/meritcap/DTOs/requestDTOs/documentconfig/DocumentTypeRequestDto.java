package com.meritcap.DTOs.requestDTOs.documentconfig;

import com.meritcap.enums.DocumentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentTypeRequestDto {

    @NotBlank(message = "Document type name is required")
    @Size(max = 150, message = "Name must be <= 150 characters")
    String name;

    @NotBlank(message = "Document type code is required")
    @Size(max = 60, message = "Code must be <= 60 characters")
    String code;

    @Size(max = 500, message = "Description must be <= 500 characters")
    String description;

    DocumentCategory category;
    Boolean allowMultiple;
    Boolean isActive;
}

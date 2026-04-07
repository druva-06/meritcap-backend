package com.meritcap.DTOs.requestDTOs.documentconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountryRequestDto {

    @NotBlank(message = "Country name is required")
    @Size(max = 100, message = "Name must be <= 100 characters")
    String name;

    @NotBlank(message = "Country code is required")
    @Size(max = 10, message = "Code must be <= 10 characters")
    String code;

    Boolean isActive;
}

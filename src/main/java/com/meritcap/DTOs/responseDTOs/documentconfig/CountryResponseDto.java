package com.meritcap.DTOs.responseDTOs.documentconfig;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountryResponseDto {
    Long id;
    String name;
    String code;
    Boolean isActive;
}

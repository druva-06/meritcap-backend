package com.consultancy.education.DTOs.requestDTOs.college;

import com.consultancy.education.enums.ActiveStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CollegeCreateRequestDto {
    @NotBlank @Size(max = 160) private String slug;
    @NotBlank @Size(max = 255) private String name;
    @Size(max = 255) private String campusName;
    @NotBlank @Size(min = 3, max = 64) private String campusCode; // immutable
    @Size(max = 100) private String country;
    @Min(1800) @Max(2100) private Integer establishedYear;
    @Size(max = 255) private String ranking;
    private String description;
    @URL @Size(max = 1024) private String websiteUrl;
    @URL @Size(max = 1024) private String collegeLogo;
    @URL @Size(max = 1024) private String campusGalleryVideoLink;
    @URL @Size(max = 1024) private String bannerUrl;
    @NotNull private ActiveStatus status;
    private Long seoId; // optional: link existing SEO row
}

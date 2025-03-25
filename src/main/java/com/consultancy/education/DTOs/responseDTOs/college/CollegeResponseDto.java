package com.consultancy.education.DTOs.responseDTOs.college;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "College response DTO for API responses")
public class CollegeResponseDto {
    @Schema(description = "Unique college identifier")
    Long collegeId;

    @Schema(description = "College name")
    String collegeName;

    @Schema(description = "Campus name")
    String campusName;

    @Schema(description = "Campus code")
    String campusCode;

    @Schema(description = "Country of the college")
    String country;

    @Schema(description = "College logo URL")
    String collegeLogo;

    @Schema(description = "College website URL")
    String websiteUrl;

    @Schema(description = "Campus video link")
    String campusGalleryVideoLink;

    @Schema(description = "Year when the college was established")
    Integer establishedYear;

    @Schema(description = "College ranking details")
    String ranking;

    @Schema(description = "College description")
    String description;
}

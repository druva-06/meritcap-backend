package com.consultancy.education.DTOs.responseDTOs.course;

import com.consultancy.education.enums.GraduationLevel;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Course response DTO for API responses")
@Builder
public class CourseResponseDto {

    @Schema(description = "Unique course identifier")
    Long courseId;

    @Schema(description = "Course name")
    String courseName;

    @Schema(description = "Department name")
    String department;

    @Schema(description = "Specialization of the course")
    String specialization;

    @Schema(description = "Graduation level (e.g., UNDERGRADUATE, POSTGRADUATE, DIPLOMA)")
    GraduationLevel graduationLevel;
}

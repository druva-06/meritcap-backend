package com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Student college course registration request dto api request")
public class StudentCollegeCourseRegistrationRequestDto {
    @NotNull(message = "Student ID is required")
    Long studentId;

    @NotNull(message = "College Course ID is required")
    Long collegeCourseId;

    @NotBlank(message = "Intake session is required")
    String intakeSession;

    String remarks;
}

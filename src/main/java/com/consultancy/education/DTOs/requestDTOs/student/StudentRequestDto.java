package com.consultancy.education.DTOs.requestDTOs.student;

import com.consultancy.education.enums.ActiveStatus;
import com.consultancy.education.enums.Gender;
import com.consultancy.education.enums.GraduationLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Student request dto api request")
public class StudentRequestDto {

    @NotNull(message = "Student Id is required")
    Long userId;

    @Pattern(regexp = "^\\d{10}$", message = "Alternate phone number must be 10 digits.")
    String alternatePhoneNumber;

    @NotNull(message = "Birth date is required.")
    @Past(message = "Birth date must be in the past.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate dateOfBirth;

    @NotNull(message = "Gender is required.")
    Gender gender;

    @NotNull(message = "Graduation level is required.")
    GraduationLevel graduationLevel;
}

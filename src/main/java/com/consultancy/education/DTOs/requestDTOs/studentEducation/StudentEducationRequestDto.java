package com.consultancy.education.DTOs.requestDTOs.studentEducation;

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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Student education request dto api request")
public class StudentEducationRequestDto {

    @NotNull(message = "Education level is required")
    GraduationLevel educationLevel;

    @NotBlank(message = "Institution name cannot be empty")
    @Size(max = 255, message = "Institution name must be less than 255 characters")
    String institutionName;

    @NotBlank(message = "Board cannot be empty")
    @Size(max = 255, message = "Board must be less than 255 characters")
    String board;

    String collegeCode;

    @NotBlank(message = "Institution address cannot be empty")
    @Size(max = 255, message = "Institution address must be less than 255 characters")
    String institutionAddress;

    @NotNull(message = "Start year is required")
    @Past(message = "Start year must be a date in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate startYear;

    @NotNull(message = "End year is required")
    @AssertTrue(message = "End year must be greater than or equal to start year")
    public boolean isEndYearValid() {
        return endYear == null || startYear == null || !endYear.isBefore(startYear);
    }
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate endYear;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    Double percentage;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10")
    Double cgpa;

    @Size(max = 50, message = "Division must be less than 50 characters")
    String division;

    @NotBlank(message = "Specialization cannot be empty")
    @Size(max = 100, message = "Specialization must be less than 100 characters")
    String specialization;

    @NotNull(message = "Backlogs is required")
    @Min(value = 0, message = "Backlogs cannot be negative")
    Integer backlogs;

    String certificate;
}

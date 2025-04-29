package com.consultancy.education.DTOs.responseDTOs.student;

import com.consultancy.education.enums.ActiveStatus;
import com.consultancy.education.enums.Gender;
import com.consultancy.education.enums.GraduationLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@Schema(description = "Student Response Dto contains student personal information.")
public class StudentResponseDto {

    @Schema(description = "UserId", example = "1")
    Long userId;

    @Schema(description = "User Alternate phone number", example = "9999999999")
    String alternatePhoneNumber;

    @Schema(description = "User Date of Birth", example = "01/01/2001")
    LocalDate dateOfBirth;

    @Schema(description = "User Gender", example = "MALE/FEMALE")
    Gender gender;

    @Schema(description = "User Graduation level", example = "UNDERGRADUATE")
    GraduationLevel graduationLevel;

    @Schema(description = "User Profile status", example = "ACTIVE")
    ActiveStatus profileActiveStatus;

    @Schema(description = "User Profile completion", example = "80")
    Integer profileCompletion;

    @Schema(description = "User aadhaar card file", example = "https://aadhaarUrl")
    String aadhaarCardFile;

    @Schema(description = "User passport file", example = "https://passportUrl")
    String passportFile;

    @Schema(description = "User pan card file", example = "https://panUrl")
    String panCardFile;

    @Schema(description = "User birth certificate file", example = "https://birthUrl")
    String birthCertificateFile;
}

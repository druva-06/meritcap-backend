package com.consultancy.education.DTOs.responseDTOs.studentEducation;

import com.consultancy.education.enums.GraduationLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Student education response dto api request")
public class StudentEducationResponseDto {

    @Schema(description = "Unique ID of the student", example = "101")
    Long userId;

    @Schema(description = "Unique ID of the education record", example = "202")
    Long educationId;

    @Schema(description = "Level of graduation", example = "BACHELORS")
    GraduationLevel educationLevel;

    @Schema(description = "Name of the institution", example = "Indian Institute of Technology")
    String institutionName;

    @Schema(description = "Board of education", example = "CBSE")
    String board;

    @Schema(description = "College code (if applicable)", example = "IIT1234")
    String collegeCode;

    @Schema(description = "Address of the institution", example = "Delhi, India")
    String institutionAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(description = "Start year of education in dd/MM/yyyy format", example = "01/06/2018")
    LocalDate startYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(description = "End year of education in dd/MM/yyyy format", example = "30/05/2022")
    LocalDate endYear;

    @Schema(description = "Percentage scored", example = "87.5")
    Double percentage;

    @Schema(description = "CGPA scored", example = "8.2")
    Double cgpa;

    @Schema(description = "Division achieved", example = "First Class")
    String division;

    @Schema(description = "Specialization or major subject", example = "Computer Science")
    String specialization;

    @Schema(description = "Number of academic backlogs", example = "0")
    Integer backlogs;

    @Schema(description = "URL or name of the uploaded education certificate", example = "https://s3.amazonaws.com/bucket/students/edu_cert.pdf")
    String certificate;
}

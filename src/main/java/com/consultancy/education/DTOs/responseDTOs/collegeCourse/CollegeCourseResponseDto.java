package com.consultancy.education.DTOs.responseDTOs.collegeCourse;

import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.DTOs.responseDTOs.course.CourseResponseDto;
import com.consultancy.education.enums.Month;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "College Course response DTO for API responses")
public class CollegeCourseResponseDto {

    @Schema(description = "College details object")
    CollegeResponseDto college;

    @Schema(description = "Course details object")
    CourseResponseDto course;

    @Schema(description = "Unique ID of the college course")
    Long collegeCourseId;

    @Schema(description = "Official course URL")
    String courseUrl;

    @Schema(description = "Tuition fee for the course (in string to allow currency symbols)")
    String tuitionFee;

    @Schema(description = "Application fee required for enrollment")
    String applicationFee;

    @Schema(description = "Course duration in months/years (as integer)")
    Integer duration;

    @Schema(description = "Range of accepted backlogs")
    String backlogAcceptanceRange;

    @Schema(description = "Eligibility criteria description")
    String eligibilityCriteria;

    @Schema(description = "Year of intake")
    Integer intakeYear;

    @Schema(description = "Intake Months")
    List<Month> intakeMonths;

    @Schema(description = "Additional remarks")
    String remarks;

    @Schema(description = "Indicates if scholarship is eligible (Yes/No)")
    String scholarshipEligible;

    @Schema(description = "Details of available scholarships")
    String scholarshipDetails;

    @Schema(description = "Minimum 10th grade score required")
    Double min10thScore;

    @Schema(description = "Minimum intermediate score required")
    Double minInterScore;

    @Schema(description = "Minimum graduation score required")
    Double minGraduationScore;

    @Schema(description = "Minimum TOEFL score")
    Double toeflMinScore;

    @Schema(description = "Minimum TOEFL band score")
    Double toeflMinBandScore;

    @Schema(description = "Minimum PTE score")
    Double pteMinScore;

    @Schema(description = "Minimum PTE band score")
    Double pteMinBandScore;

    @Schema(description = "Minimum IELTS score")
    Double ieltsMinScore;

    @Schema(description = "Minimum IELTS band score")
    Double ieltsMinBandScore;

    @Schema(description = "Minimum SAT score")
    Double satMinScore;

    @Schema(description = "Minimum GRE score")
    Double greMinScore;

    @Schema(description = "Minimum GMAT score")
    Double gmatMinScore;

    @Schema(description = "Minimum Duolingo English Test (DET) score")
    Double detMinScore;

    @Schema(description = "Minimum CAT score")
    Double catMinScore;
}

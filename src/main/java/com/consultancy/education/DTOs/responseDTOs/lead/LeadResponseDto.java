package com.consultancy.education.DTOs.responseDTOs.lead;

import com.consultancy.education.enums.LeadStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Lead response DTO")
public class LeadResponseDto {

    @Schema(description = "Lead ID")
    Long id;

    // Personal Information
    @Schema(description = "First name")
    String firstName;

    @Schema(description = "Last name")
    String lastName;

    @Schema(description = "Email address")
    String email;

    @Schema(description = "Phone number")
    String phoneNumber;

    @Schema(description = "Country")
    String country;

    // Lead Management
    @Schema(description = "Lead status")
    LeadStatus status;

    @Schema(description = "Lead score (0-100)")
    Integer score;

    @Schema(description = "Lead source")
    String leadSource;

    // Preferences
    @Schema(description = "Preferred countries")
    String preferredCountries;

    @Schema(description = "Preferred courses")
    String preferredCourses;

    @Schema(description = "Budget range")
    String budgetRange;

    @Schema(description = "Intake")
    String intake;

    @Schema(description = "Tags")
    List<String> tags;

    // Assignment
    @Schema(description = "Assigned counselor ID")
    Long assignedToId;

    @Schema(description = "Assigned counselor name")
    String assignedToName;

    @Schema(description = "Created by user ID")
    Long createdById;

    @Schema(description = "Created by user name")
    String createdByName;

    // Encrypted data (returned as-is, frontend will decrypt if needed)
    @Schema(description = "Encrypted personal details")
    String encryptedPersonalDetails;

    @Schema(description = "Encrypted academic details")
    String encryptedAcademicDetails;

    @Schema(description = "Encrypted preferences")
    String encryptedPreferences;

    // Duplicate info
    @Schema(description = "Is this lead marked as duplicate")
    Boolean isDuplicate;

    @Schema(description = "ID of original lead if this is a duplicate")
    Long duplicateOf;

    // Metadata
    @Schema(description = "Created timestamp")
    LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    LocalDateTime updatedAt;
}

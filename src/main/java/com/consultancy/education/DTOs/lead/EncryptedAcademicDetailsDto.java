package com.consultancy.education.DTOs.lead;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * This DTO represents the structure of academic details that will be encrypted
 * by the frontend
 * and sent as a JSON string in the 'encryptedAcademicDetails' field.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EncryptedAcademicDetailsDto {
    String currentEducationLevel; // DIPLOMA, HIGH_SCHOOL, UNDERGRADUATE, POSTGRADUATE, PHD, etc.
    String degreeCourse;
    String universityCollege;
    String percentageCGPA;
    String yearOfPassing;
    String workExperience;

    // Test Scores
    String ieltsScore;
    String toeflScore;
    String greScore;
    String gmatScore;
}

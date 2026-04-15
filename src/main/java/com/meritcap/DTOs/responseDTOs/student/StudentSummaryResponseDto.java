package com.meritcap.DTOs.responseDTOs.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentSummaryResponseDto {

    // Identity
    private Long userId;
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String gender;
    private String dateOfBirth;
    private String graduationLevel;
    private String profileActiveStatus;
    private Integer profileCompletion;

    // Application analytics
    private long totalApplications;
    private long pendingApplications;
    private long submittedApplications;
    private long approvedApplications;
    private long rejectedApplications;

    // Document analytics
    private long totalDocuments;
    private long pendingDocuments;
    private long verifiedDocuments;

    // Wishlist
    private long wishlistCount;

    // Education records count
    private long educationRecordsCount;
}

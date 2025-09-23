package com.consultancy.education.DTOs.responseDTOs.wishlistItem;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistItemResponse {
    private Long wishlistItemId;
    private Long studentId;
    private Long collegeCourseId;
    private String collegeName;
    private String courseName;
    private String campusName;
    private String tuitionFee;
    // Optionally: add college/course details for richer UI later
}
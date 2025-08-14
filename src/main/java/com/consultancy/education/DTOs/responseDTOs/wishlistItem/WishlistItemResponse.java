package com.consultancy.education.DTOs.responseDTOs.wishlistItem;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistItemResponse {
    private Long wishlistItemId;
    private Long collegeCourseId;
    private Long studentId;
    // Optionally: add college/course details for richer UI later
}
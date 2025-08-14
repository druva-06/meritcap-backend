package com.consultancy.education.DTOs.requestDTOs.wishlistItem;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddWishlistItemRequest {
    @NotNull(message = "collegeCourseId is required")
    private Long collegeCourseId;
}
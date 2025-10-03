package com.consultancy.education.DTOs.responseDTOs.wishlistItem;

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
@Schema(description = "Wishlist response DTO for API responses")
public class WishlistItemResponse {
    private Long wishlistItemId;
    private Long studentId;
    private Long collegeCourseId;
    private String collegeName;
    private String courseName;
    private String campusName;
    private String tuitionFee;
    List<Month> intakeMonths;
    // Optionally: add college/course details for richer UI later
}
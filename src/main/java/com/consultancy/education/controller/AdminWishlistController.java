package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.wishlistItem.AddWishlistItemRequest;
import com.consultancy.education.DTOs.responseDTOs.wishlistItem.WishlistItemResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/wishlists")
@RequiredArgsConstructor
@Slf4j
public class AdminWishlistController {

    private final WishlistService wishlistService;

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping
    public ResponseEntity<?> getStudentWishlist(@RequestParam Long studentId) {
        log.info("ADMIN/COUNSELOR requested wishlist for student {}", studentId);
        try {
            List<WishlistItemResponse> items = wishlistService.getWishlistItems(studentId);
            log.info("Found {} wishlist items for student {} (admin view)", items.size(), studentId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(items, "Wishlist items fetched", 200));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch wishlist for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error while fetching wishlist for student {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @PostMapping("/{studentId}/items")
    public ResponseEntity<?> addWishlistItemForStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody AddWishlistItemRequest request) {
        log.info("ADMIN/COUNSELOR adding course {} to student {}'s wishlist", request.getCollegeCourseId(), studentId);
        try {
            WishlistItemResponse response = wishlistService.addWishlistItem(studentId, request);
            log.info("Added course {} to wishlist for student {} (admin/counselor)", request.getCollegeCourseId(), studentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Item added to wishlist", 201));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add course {} to wishlist for student {}: {}", request.getCollegeCourseId(), studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error while adding course {} to wishlist for student {}: {}", request.getCollegeCourseId(), studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @DeleteMapping("/{studentId}/items/{wishlistItemId}")
    public ResponseEntity<?> removeWishlistItemForStudent(
            @PathVariable Long studentId,
            @PathVariable Long wishlistItemId) {
        log.info("ADMIN/COUNSELOR removing wishlist item {} for student {}", wishlistItemId, studentId);
        try {
            wishlistService.removeWishlistItem(studentId, wishlistItemId);
            log.info("Removed wishlist item {} for student {} (admin/counselor)", wishlistItemId, studentId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Wishlist item removed", 200));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove wishlist item {} for student {}: {}", wishlistItemId, studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Error while removing wishlist item {} for student {}: {}", wishlistItemId, studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }
}


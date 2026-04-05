package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.wishlistItem.AddWishlistItemRequest;
import com.meritcap.DTOs.responseDTOs.wishlistItem.WishlistItemResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/students/{studentId}/wishlist/items")
@RequiredArgsConstructor
@Slf4j // Lombok annotation for logger
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addWishlistItem(
            @PathVariable Long studentId,
            @Valid @RequestBody AddWishlistItemRequest request) {
        log.info("Received request to add course {} to student {}'s wishlist", request.getCollegeCourseId(), studentId);
        try {
            authenticatedUserResolver.assertCurrentStudentOwns(studentId);
            WishlistItemResponse response = wishlistService.addWishlistItem(studentId, request);
            log.info("Successfully added course {} to wishlist for student {}", request.getCollegeCourseId(),
                    studentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "Item added to wishlist", 201));
        } catch (com.meritcap.exception.CustomException e) {
            log.warn("Forbidden wishlist add access for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add course {} to wishlist for student {}: {}", request.getCollegeCourseId(), studentId,
                    e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error while adding course {} to wishlist for student {}: {}",
                    request.getCollegeCourseId(), studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getWishlistItems(@PathVariable Long studentId) {
        log.info("Received request to fetch wishlist items for student {}", studentId);
        try {
            authenticatedUserResolver.assertCurrentStudentOwns(studentId);
            List<WishlistItemResponse> items = wishlistService.getWishlistItems(studentId);
            log.info("Fetched {} wishlist items for student {}", items.size(), studentId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(items, "Wishlist items fetched", 200));
        } catch (com.meritcap.exception.CustomException e) {
            log.warn("Forbidden wishlist read access for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch wishlist for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error while fetching wishlist for student {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }

    @DeleteMapping("/{wishlistItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeWishlistItem(
            @PathVariable Long studentId,
            @PathVariable Long wishlistItemId) {
        log.info("Received request to remove wishlist item {} for student {}", wishlistItemId, studentId);
        try {
            authenticatedUserResolver.assertCurrentStudentOwns(studentId);
            wishlistService.removeWishlistItem(studentId, wishlistItemId);
            log.info("Removed wishlist item {} for student {}", wishlistItemId, studentId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Wishlist item removed", 200));
        } catch (com.meritcap.exception.CustomException e) {
            log.warn("Forbidden wishlist delete access for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove wishlist item {} for student {}: {}", wishlistItemId, studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error while removing wishlist item {} for student {}: {}", wishlistItemId, studentId,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getWishlistCount(@PathVariable Long studentId) {
        log.info("Received request to count wishlist items for student {}", studentId);
        try {
            authenticatedUserResolver.assertCurrentStudentOwns(studentId);
            int count = wishlistService.getWishlistItemCount(studentId);
            log.info("Student {} has {} items in wishlist", studentId, count);
            return ResponseEntity.ok(new ApiSuccessResponse<>(count, "Wishlist item count fetched", 200));
        } catch (com.meritcap.exception.CustomException e) {
            log.warn("Forbidden wishlist count access for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 403));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to count wishlist items for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error while counting wishlist for student {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Internal server error", 500));
        }
    }
}

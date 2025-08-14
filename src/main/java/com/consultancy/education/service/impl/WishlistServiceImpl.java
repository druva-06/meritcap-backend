package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.wishlistItem.AddWishlistItemRequest;
import com.consultancy.education.DTOs.responseDTOs.wishlistItem.WishlistItemResponse;
import com.consultancy.education.model.CollegeCourse;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.Wishlist;
import com.consultancy.education.model.WishlistItem;
import com.consultancy.education.repository.CollegeCourseRepository;
import com.consultancy.education.repository.StudentRepository;
import com.consultancy.education.repository.WishlistItemRepository;
import com.consultancy.education.repository.WishlistRepository;
import com.consultancy.education.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // <-- Lombok annotation for logger
public class WishlistServiceImpl implements WishlistService {

    private final StudentRepository studentRepository;
    private final CollegeCourseRepository collegeCourseRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Override
    @Transactional
    public WishlistItemResponse addWishlistItem(Long studentId, AddWishlistItemRequest request) {
        log.info("Attempting to add course {} to wishlist for student {}", request.getCollegeCourseId(), studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found: {}", studentId);
                    return new IllegalArgumentException("Student not found");
                });

        CollegeCourse course = collegeCourseRepository.findById(request.getCollegeCourseId())
                .orElseThrow(() -> {
                    log.warn("College course not found: {}", request.getCollegeCourseId());
                    return new IllegalArgumentException("College course not found");
                });

        Wishlist wishlist = wishlistRepository.findByStudent(student)
                .orElseGet(() -> {
                    log.info("No wishlist found for student {}. Creating a new one.", studentId);
                    Wishlist newWishlist = Wishlist.builder().student(student).build();
                    return wishlistRepository.save(newWishlist);
                });

        if (wishlistItemRepository.existsByWishlistAndCollegeCourse(wishlist, course)) {
            log.warn("Duplicate wishlist entry: student {}, course {}", studentId, request.getCollegeCourseId());
            throw new IllegalArgumentException("This course is already in the wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .collegeCourse(course)
                .build();
        item = wishlistItemRepository.save(item);

        log.info("Course {} added to wishlist for student {} (wishlistItemId={})", course.getId(), student.getId(), item.getId());

        return WishlistItemResponse.builder()
                .wishlistItemId(item.getId())
                .collegeCourseId(course.getId())
                .studentId(student.getId())
                .build();
    }

    @Override
    public List<WishlistItemResponse> getWishlistItems(Long studentId) {
        log.info("Fetching wishlist items for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found: {}", studentId);
                    return new IllegalArgumentException("Student not found");
                });

        Wishlist wishlist = wishlistRepository.findByStudent(student)
                .orElseThrow(() -> {
                    log.warn("Wishlist not found for student {}", studentId);
                    return new IllegalArgumentException("Wishlist not found");
                });

        List<WishlistItem> items = wishlistItemRepository.findByWishlist(wishlist);
        log.debug("Found {} wishlist items for student {}", items.size(), studentId);

        return items.stream().map(item ->
                WishlistItemResponse.builder()
                        .wishlistItemId(item.getId())
                        .collegeCourseId(item.getCollegeCourse().getId())
                        .studentId(studentId)
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeWishlistItem(Long studentId, Long wishlistItemId) {
        log.info("Attempting to remove wishlist item {} for student {}", wishlistItemId, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found: {}", studentId);
                    return new IllegalArgumentException("Student not found");
                });

        Wishlist wishlist = wishlistRepository.findByStudent(student)
                .orElseThrow(() -> {
                    log.warn("Wishlist not found for student {}", studentId);
                    return new IllegalArgumentException("Wishlist not found");
                });

        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> {
                    log.warn("Wishlist item not found: {}", wishlistItemId);
                    return new IllegalArgumentException("Wishlist item not found");
                });

        if (!item.getWishlist().getId().equals(wishlist.getId())) {
            log.warn("Wishlist item {} does not belong to student {}", wishlistItemId, studentId);
            throw new IllegalArgumentException("This wishlist item does not belong to the student");
        }

        wishlistItemRepository.delete(item);
        log.info("Wishlist item {} removed for student {}", wishlistItemId, studentId);
    }

    @Override
    public int getWishlistItemCount(Long studentId) {
        log.info("Counting wishlist items for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found: {}", studentId);
                    return new IllegalArgumentException("Student not found");
                });

        Wishlist wishlist = wishlistRepository.findByStudent(student)
                .orElseThrow(() -> {
                    log.warn("Wishlist not found for student {}", studentId);
                    return new IllegalArgumentException("Wishlist not found");
                });

        int count = wishlistItemRepository.findByWishlist(wishlist).size();
        log.info("Student {} has {} items in wishlist", studentId, count);

        return count;
    }
}
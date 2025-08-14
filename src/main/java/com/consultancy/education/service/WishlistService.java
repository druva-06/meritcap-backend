package com.consultancy.education.service;


import com.consultancy.education.DTOs.requestDTOs.wishlistItem.AddWishlistItemRequest;
import com.consultancy.education.DTOs.responseDTOs.wishlistItem.WishlistItemResponse;

import java.util.List;

public interface WishlistService {
    WishlistItemResponse addWishlistItem(Long studentId, AddWishlistItemRequest request);

    List<WishlistItemResponse> getWishlistItems(Long studentId);

    void removeWishlistItem(Long studentId, Long wishlistItemId);

    int getWishlistItemCount(Long studentId);
}

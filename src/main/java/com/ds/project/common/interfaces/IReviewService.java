package com.ds.project.common.interfaces;

import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.ReviewFilterRequest;
import com.ds.project.common.entities.dto.request.ReviewRequest;
import com.ds.project.common.entities.dto.response.ReviewResponse;

public interface IReviewService {
    ReviewResponse createReview(String userId, ReviewRequest request);
    ReviewResponse updateReview(String userId, String reviewId, ReviewRequest request);
    void deleteReview(String userId, String reviewId);
    ReviewResponse getReviewById(String id);
    PaginationResponse<ReviewResponse> getAllReviews(ReviewFilterRequest filter);
    PaginationResponse<ReviewResponse> getReviewsByProduct(String productId, ReviewFilterRequest filter);
    PaginationResponse<ReviewResponse> getReviewsByCurrentUser(String userId, ReviewFilterRequest filter);
}

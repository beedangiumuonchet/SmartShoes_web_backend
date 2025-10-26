package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Review;
import com.ds.project.common.entities.dto.response.ReviewResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse mapToDto(Review review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .comment(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

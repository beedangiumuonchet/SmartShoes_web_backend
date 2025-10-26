package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.Review;
import com.ds.project.app_context.models.User;
import com.ds.project.app_context.repositories.ProductRepository;
import com.ds.project.app_context.repositories.ReviewRepository;
import com.ds.project.app_context.repositories.UserRepository;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.ReviewFilterRequest;
import com.ds.project.common.entities.dto.request.ReviewRequest;
import com.ds.project.common.entities.dto.response.ReviewResponse;
import com.ds.project.common.interfaces.IReviewService;
import com.ds.project.common.mapper.ReviewMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(String userId, ReviewRequest request) {
        try {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Review review = Review.builder()
                    .product(product)
                    .user(user)
                    .comment(request.getComment())
                    .rating(request.getRating())
                    .build();

            Review saved = reviewRepository.save(review);
            return reviewMapper.mapToDto(saved);
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create review: " + e.getMessage());
        }
    }

    @Override
    public PaginationResponse<ReviewResponse> getReviewsByCurrentUser(String userId, ReviewFilterRequest filter) {
        String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
        String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔒 Lọc theo user hiện tại
            predicates.add(cb.equal(root.join("user", JoinType.INNER).get("id"), userId));

            if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                String kw = "%" + filter.getQ().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("comment")), kw));
            }

            if (filter.getProductId() != null && !filter.getProductId().isEmpty()) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getMinRating()));
            }
            if (filter.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), filter.getMaxRating()));
            }

            if (filter.getCreatedDate_from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_from().atStartOfDay()));
            }
            if (filter.getCreatedDate_to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_to().atTime(23,59,59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var page = reviewRepository.findAll(spec, pageable);
        List<ReviewResponse> content = page.getContent().stream().map(reviewMapper::mapToDto).toList();

        return PaginationResponse.<ReviewResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(String userId, String reviewId, ReviewRequest request) {
        try {
            Review existing = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            // Chỉ owner mới được update (hoặc admin tuỳ nhu cầu)
            if (!existing.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized to update this review");
            }

            // Nếu user muốn đổi product (hiếm dùng) — kiểm tra product tồn tại
            if (request.getProductId() != null && !request.getProductId().equals(existing.getProduct().getId())) {
                Product product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                existing.setProduct(product);
            }

            existing.setComment(request.getComment());
            existing.setRating(request.getRating());

            Review saved = reviewRepository.save(existing);
            return reviewMapper.mapToDto(saved);
        } catch (Exception e) {
            log.error("Error updating review {}: {}", reviewId, e.getMessage(), e);
            throw new RuntimeException("Failed to update review: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteReview(String userId, String reviewId) {
        try {
            Review existing = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));

            if (!existing.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized to delete this review");
            }

            reviewRepository.delete(existing);
        } catch (Exception e) {
            log.error("Error deleting review {}: {}", reviewId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete review: " + e.getMessage());
        }
    }

    @Override
    public ReviewResponse getReviewById(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return reviewMapper.mapToDto(review);
    }

    @Override
    public PaginationResponse<ReviewResponse> getAllReviews(ReviewFilterRequest filter) {
        String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
        String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                String kw = "%" + filter.getQ().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("comment")), kw));
            }

            if (filter.getProductId() != null && !filter.getProductId().isEmpty()) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getMinRating()));
            }
            if (filter.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), filter.getMaxRating()));
            }

            if (filter.getCreatedDate_from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_from().atStartOfDay()));
            }
            if (filter.getCreatedDate_to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_to().atTime(23,59,59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var page = reviewRepository.findAll(spec, pageable);
        List<ReviewResponse> content = page.getContent().stream().map(reviewMapper::mapToDto).toList();

        return PaginationResponse.<ReviewResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    public PaginationResponse<ReviewResponse> getReviewsByProduct(String productId, ReviewFilterRequest filter) {
        // bắt buộc filter.productId = productId
        filter.setProductId(productId);
        return getAllReviews(filter);
    }
}

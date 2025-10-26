package com.ds.project.application.controllers.v1;

import com.ds.project.application.annotations.AuthRequired;
import com.ds.project.business.v1.services.ReviewService;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.ReviewFilterRequest;
import com.ds.project.common.entities.dto.request.ReviewRequest;
import com.ds.project.common.entities.dto.response.ReviewResponse;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof com.ds.project.common.entities.common.UserPayload payload) {
            return payload.getUserId();
        }
        return null;
    }

    @PostMapping
    @AuthRequired
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            log.warn("❌ Unauthorized attempt to create review");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            ReviewResponse resp = reviewService.createReview(userId, request);
            log.info("✅ User [{}] created review successfully for product [{}]", userId, request.getProductId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to create review by user [{}]: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create review: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews(@ModelAttribute ReviewFilterRequest filter) {
        try {
            var resp = reviewService.getAllReviews(filter);
            log.info("✅ Fetched {} reviews (page {} of {})",
                    resp.getContent().size(), resp.getPage() + 1, resp.getTotalPages());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to fetch reviews: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch reviews: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable String id) {
        try {
            ReviewResponse resp = reviewService.getReviewById(id);
            log.info("✅ Fetched review [{}] successfully", id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to get review [{}]: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to get review: " + e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable String productId, @ModelAttribute ReviewFilterRequest filter) {
        try {
            var resp = reviewService.getReviewsByProduct(productId, filter);
            log.info("✅ Fetched {} reviews for product [{}]", resp.getContent().size(), productId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to get reviews by product [{}]: {}", productId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to get reviews by product: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @AuthRequired
    public ResponseEntity<?> getMyReviews(@ModelAttribute ReviewFilterRequest filter) {
        String userId = getCurrentUserId();
        if (userId == null) {
            log.warn("❌ Unauthorized attempt to fetch own reviews");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            var resp = reviewService.getReviewsByCurrentUser(userId, filter);
            log.info("✅ User [{}] fetched {} own reviews", userId, resp.getContent().size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to fetch reviews by current user [{}]: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch my reviews: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @AuthRequired
    public ResponseEntity<?> updateReview(@PathVariable String id, @Valid @RequestBody ReviewRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            log.warn("❌ Unauthorized attempt to update review [{}]", id);
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            ReviewResponse resp = reviewService.updateReview(userId, id, request);
            log.info("✅ User [{}] updated review [{}] successfully", userId, id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("❌ Failed to update review [{}] by user [{}]: {}", id, userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update review: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @AuthRequired
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            log.warn("❌ Unauthorized attempt to delete review [{}]", id);
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            reviewService.deleteReview(userId, id);
            log.info("✅ User [{}] deleted review [{}] successfully", userId, id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (Exception e) {
            log.error("❌ Failed to delete review [{}] by user [{}]: {}", id, userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to delete review: " + e.getMessage());
        }
    }
}

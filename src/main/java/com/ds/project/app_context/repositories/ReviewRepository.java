package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String>, JpaSpecificationExecutor<Review> {
    List<Review> findByProductId(String productId); // optional helper
    List<Review> findByUserId(String userId);
}

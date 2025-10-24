package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, String> {
}

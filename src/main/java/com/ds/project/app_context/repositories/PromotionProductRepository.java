package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.PromotionProduct;
import com.ds.project.app_context.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, String> {

    /**
     * Lấy tất cả PromotionProduct theo productVariantId
     */
    List<PromotionProduct> findByProductVariantId(String productVariantId);

    /**
     * Lấy các PromotionProduct đang active theo productVariantId
     */
    @Query("""
        SELECT pp FROM PromotionProduct pp
        WHERE pp.productVariant.id = :variantId
          AND pp.promotion.status = 'ACTIVE'
          AND :today BETWEEN pp.promotion.startDate AND pp.promotion.endDate
    """)
    List<PromotionProduct> findActiveByVariantId(@Param("variantId") String variantId, @Param("today") LocalDate today);

    /**
     * Lấy tất cả PromotionProduct theo promotionId
     */
    List<PromotionProduct> findByPromotionId(String promotionId);
}

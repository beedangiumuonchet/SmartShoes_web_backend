package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String>{
    List<ProductVariant> findByProductId(String productId);

    boolean existsByProductIdAndColorIdAndSize(String productId, String colorId, String size);
}

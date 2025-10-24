package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, String> {
    List<ProductAttribute> findByProduct_Id(String productId);
    boolean existsByProduct_IdAndAttribute_Id(String productId, String attributeId);
}

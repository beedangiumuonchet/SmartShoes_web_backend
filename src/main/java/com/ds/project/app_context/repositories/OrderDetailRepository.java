package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.OrderDetail;
import com.ds.project.app_context.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    // Kiểm tra xem có OrderDetail nào dùng variant này không
    boolean existsByProductVariant(ProductVariant productVariant);
}

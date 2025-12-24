package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.OrderDetail;
import com.ds.project.app_context.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    // Kiểm tra xem có OrderDetail nào dùng variant này không
    boolean existsByProductVariant(ProductVariant productVariant);
    @Query("SELECT od.productVariant.product AS product, SUM(od.quantity) AS totalQuantity, " +
            "SUM(od.quantity * od.price) AS totalRevenue " +
            "FROM OrderDetail od " +
            "WHERE od.order.updatedAt BETWEEN :start AND :end " +
            "AND od.order.status = com.ds.project.common.enums.OrderStatus.DELIVERED " +
            "GROUP BY od.productVariant.product " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopProductsDelivered(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}

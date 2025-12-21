package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);
    @Query("SELECT o.user AS user, " +
            "COUNT(o) AS totalOrders, " +
            "SUM(o.totalAmount) AS totalSpent " +
            "FROM Order o " +
            "WHERE o.updatedAt BETWEEN :start AND :end " +
            "AND o.status = com.ds.project.common.enums.OrderStatus.DELIVERED " +
            "GROUP BY o.user " +
            "ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersDelivered(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}

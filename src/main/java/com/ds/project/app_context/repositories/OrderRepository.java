package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);
}

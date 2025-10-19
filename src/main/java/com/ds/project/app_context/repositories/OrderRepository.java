package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser(User user);
}

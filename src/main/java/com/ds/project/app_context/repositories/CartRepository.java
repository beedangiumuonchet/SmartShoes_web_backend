package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Cart;
import com.ds.project.app_context.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUser(User user);
}

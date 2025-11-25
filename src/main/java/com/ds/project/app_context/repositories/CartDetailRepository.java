package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Cart;
import com.ds.project.app_context.models.CartDetail;
import com.ds.project.app_context.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartDetailRepository extends JpaRepository<CartDetail, String> {
    List<CartDetail> findByCart(Cart cart);
    Optional<CartDetail> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
    void deleteByCart(Cart cart);
    boolean existsByProductVariant(ProductVariant productVariant);
}

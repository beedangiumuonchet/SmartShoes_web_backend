package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrder_Id(String orderId);
}

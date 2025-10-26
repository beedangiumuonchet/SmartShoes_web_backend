package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrder_Id(String orderId);
}

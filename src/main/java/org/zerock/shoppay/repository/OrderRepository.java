package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Order;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByPaymentKey(String paymentKey);
}
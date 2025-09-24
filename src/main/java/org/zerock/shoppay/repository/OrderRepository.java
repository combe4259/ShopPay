package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.Entity.Member;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByPaymentKey(String paymentKey);
    
    // 회원의 주문 내역 조회 (최신순)
    List<Order> findByMemberOrderByCreatedAtDesc(Member member);
    
    // 회원의 주문 내역 조회 (OrderItem과 Product 함께 로드)
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE o.member = :member " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByMemberWithItems(@Param("member") Member member);
}
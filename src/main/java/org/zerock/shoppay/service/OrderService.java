package org.zerock.shoppay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.*;
import org.zerock.shoppay.repository.CartItemRepository;
import org.zerock.shoppay.repository.OrderRepository;
import org.zerock.shoppay.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;

    // 장바구니 상품들로 주문 생성 (결제 전, PENDING 상태)
    @Transactional
    public Order createOrderFromCart(Member member, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
        }

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);

        if (cartItems.isEmpty() || cartItemIds.size() != cartItems.size()) {
            throw new IllegalArgumentException("주문 상품 정보를 찾을 수 없거나 유효하지 않은 상품이 포함되어 있습니다.");
        }

        // 1단계: 재고 차감 (Native Query로 동시성 제어 - 성능 최적화)
        // 여기서 차감하므로 다른 사용자가 동시에 구매할 수 없음
        for (CartItem cartItem : cartItems) {
            productService.decreaseStockWithNativeQuery(
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            );
        }

        // 주문 총액 계산 (서버에서 직접 계산)
        int totalAmount = cartItems.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();

        // 주문 엔티티 생성
        Order order = Order.builder()
                .orderId("ORDER_" + member.getId() + "_" + System.currentTimeMillis()) // 고유 ID 생성
                .member(member)
                .customerName(member.getName())
                .customerEmail(member.getEmail())
                .customerPhone(member.getPhone())
                .totalAmount(totalAmount)
                .status("PENDING") // 결제 대기 상태
                .build();

        // 재고 예약 만료 시간 설정 (15분)
        order.setReservedUntil(LocalDateTime.now().plusMinutes(15));

        // 주문 아이템 엔티티 생성 및 주문에 추가
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice())
                    .build();
            order.addOrderItem(orderItem);
        }

        // 생성된 주문을 DB에 저장하고 반환
        return orderRepository.save(order);
    }

    // 결제 완료 처리 (금액 검증 포함)
    @Transactional
    public Order confirmPayment(String orderId, String paymentKey, Long amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        // 1. 이미 처리된 주문인지 확인
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("이미 처리된 주문입니다.");
        }

        // 2. 결제 금액 검증 (중요)
        if (!order.getTotalAmount().equals(amount.intValue())) {
            // TODO: 금액 위변조 시도가 의심되므로 결제 취소 API를 호출하는 로직 추가 필요
            throw new RuntimeException("주문 금액이 일치하지 않습니다. 결제 위변조가 의심됩니다.");
        }

        // 3. 주문 상태 업데이트
        order.setPaymentKey(paymentKey);
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());

        // ⭐ 2단계: 재고는 이미 createOrderFromCart()에서 차감했으므로 여기서는 하지 않음!
        // 결제 완료 시점에는 상태만 변경

        return orderRepository.save(order);
    }

    // 주문 취소 및 재고 복구 (프론트에서 이탈 시 또는 스케줄러에서 호출)
    @Transactional
    public Order cancelOrderAndRestoreStock(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + orderId));

        // 이미 취소된 주문이면 무시
        if ("CANCELLED".equals(order.getStatus())) {
            return order;
        }

        // 이미 결제 완료된 주문은 취소 불가
        if ("PAID".equals(order.getStatus())) {
            throw new RuntimeException("결제 완료된 주문은 환불 절차가 필요합니다");
        }

        // PENDING 상태만 취소 가능
        if ("PENDING".equals(order.getStatus())) {
            // 재고 복구
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
                );
            }

            // 주문 취소
            order.setStatus("CANCELLED");
            System.out.println("주문 취소 및 재고 복구 완료: " + orderId);
        }

        return orderRepository.save(order);
    }

    // 주문 조회
    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    // 결제키로 주문 조회
    public Optional<Order> findByPaymentKey(String paymentKey) {
        return orderRepository.findByPaymentKey(paymentKey);
    }
}

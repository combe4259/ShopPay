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

        // 재고 확인
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct().getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("재고가 부족한 상품이 있습니다: " + cartItem.getProduct().getName());
            }
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

        // 4. 재고 감소
        for (OrderItem item : order.getOrderItems()) {
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }

        return orderRepository.save(order);
    }

    // 주문 취소
    public Order cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Order already cancelled");
        }

        // 재고 복구
        if ("PAID".equals(order.getStatus())) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus("CANCELLED");
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

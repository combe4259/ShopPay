package org.zerock.shoppay.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.Entity.OrderItem;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.OrderRepository;
import org.zerock.shoppay.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    
    // 주문 생성 (결제 전 상태)
    public Order createOrder(String orderId, Long productId, Integer quantity, 
                           String customerName, String customerEmail, String customerPhone) {
        //상품 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 재고 확인
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        // 주문 생성
        Order order = Order.builder()
                .orderId(orderId)
                .totalAmount(product.getPrice() * quantity)
                .status("PENDING")
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .build();
        
        // 주문 아이템 추가
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();
        
        order.addOrderItem(orderItem);
        
        return orderRepository.save(order);
    }
    
    // 결제 완료 처리
    public Order confirmPayment(String orderId, String paymentKey, JSONObject paymentData) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // 이미 처리된 주문인지 확인
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Order already processed");
        }
        
        // 주문 상태 업데이트
        order.setPaymentKey(paymentKey);
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        
        // 재고 감소
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
    
    // 주문 저장 (장바구니에서 주문 생성 시)
    @Transactional
    public Order saveOrder(Order order) {
        // 재고 감소
        for (OrderItem item : order.getOrderItems()) {
            productService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }
        return orderRepository.save(order);
    }
}
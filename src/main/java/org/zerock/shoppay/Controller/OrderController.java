package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.service.OrderService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    // 주문 생성 API (결제 전)
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String customerPhone) {
        
        try {
            // 고유한 주문 ID 생성
            String orderId = "order_" + productId + "_" + System.currentTimeMillis();
            
            // 주문 생성
            Order order = orderService.createOrder(
                orderId, productId, quantity,
                customerName, customerEmail, customerPhone
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getOrderId());
            response.put("amount", order.getTotalAmount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // 주문 상태 확인
    @GetMapping("/status/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable String orderId) {
        try {
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getOrderId());
            response.put("status", order.getStatus());
            response.put("amount", order.getTotalAmount());
            response.put("paidAt", order.getPaidAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
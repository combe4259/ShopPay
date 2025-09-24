package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.CartItem;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.service.CartService;
import org.zerock.shoppay.service.MemberService;
import org.zerock.shoppay.service.OrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final CartService cartService;
    private final MemberService memberService;

    @Value("${toss.client.key}")
    private String TOSS_CLIENT_KEY;
    
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
    
    // 장바구니 결제 페이지
    @GetMapping("/checkout")
    public String checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<Long> items,
            Model model) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        Member member = memberService.findByEmail(userDetails.getUsername());
        Cart cart = cartService.getCartWithItems(member);
        
        // 선택된 아이템만 필터링 (items 파라미터가 있는 경우)
        List<CartItem> selectedItems = new ArrayList<>();
        int totalPrice = 0;
        
        if (items != null && !items.isEmpty()) {
            // 선택된 아이템만
            for (CartItem item : cart.getCartItems()) {
                if (items.contains(item.getId())) {
                    selectedItems.add(item);
                    totalPrice += item.getTotalPrice();
                }
            }
        } else {
            // 전체 아이템
            selectedItems = cart.getCartItems();
            totalPrice = cart.getTotalPrice();
        }
        
        // 배송비 계산
        int deliveryFee = totalPrice >= 50000 ? 0 : 5000;
        int finalTotal = totalPrice + deliveryFee;
        
        // JavaScript에서 사용할 간단한 데이터 구조 생성
        List<Map<String, Object>> cartItemsData = new ArrayList<>();
        for (CartItem item : selectedItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("totalPrice", item.getTotalPrice());
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", item.getProduct().getId());
            productData.put("name", item.getProduct().getName());
            productData.put("price", item.getProduct().getPrice());
            itemData.put("product", productData);
            cartItemsData.add(itemData);
        }
        
        Map<String, Object> memberData = new HashMap<>();
        if (member != null) {
            memberData.put("email", member.getEmail());
            memberData.put("name", member.getName());
        }
        
        model.addAttribute("member", member);
        model.addAttribute("cartItems", selectedItems);
        model.addAttribute("cartItemsJson", cartItemsData);
        model.addAttribute("memberJson", memberData);
        model.addAttribute("subtotal", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("totalAmount", finalTotal);
        model.addAttribute("clientKey", TOSS_CLIENT_KEY);

        return "order/checkout";
    }
    
    // 장바구니 주문 생성 (AJAX)
    @PostMapping("/create-from-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }
            
            Member member = memberService.findByEmail(userDetails.getUsername());
            List<Integer> cartItemIds = (List<Integer>) requestData.get("cartItemIds");
            
            // 선택된 카트 아이템들로 주문 생성
            String orderId = "ORDER_" + System.currentTimeMillis();
            int totalAmount = 0;
            
            // 선택된 아이템들의 총 금액 계산
            Cart cart = cartService.getCartWithItems(member);
            List<CartItem> selectedItems = new ArrayList<>();
            
            for (CartItem item : cart.getCartItems()) {
                if (cartItemIds.contains(item.getId().intValue())) {
                    selectedItems.add(item);
                    totalAmount += item.getTotalPrice();
                }
            }
            
            if (selectedItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "선택된 상품이 없습니다.");
                return ResponseEntity.ok(response);
            }
            
            // 세션에 선택된 아이템 ID 저장 (결제 성공 후 처리용)
            // 실제로는 DB에 임시 주문으로 저장하는 것이 좋음
            
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("totalAmount", totalAmount);
            response.put("selectedItemIds", cartItemIds);
            response.put("message", "주문이 생성되었습니다.");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
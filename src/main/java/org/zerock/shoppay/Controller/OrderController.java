package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.CartItem;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.dto.CheckoutItemDto;
import org.zerock.shoppay.dto.CreateOrderRequestDto;
import org.zerock.shoppay.service.CartService;
import org.zerock.shoppay.service.MemberService;
import org.zerock.shoppay.service.OrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final MemberService memberService;

    @Value("${toss.client.key}")
    private String TOSS_CLIENT_KEY;

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

        // DTO를 사용하여 JavaScript에서 사용할 데이터 구조 생성
        List<CheckoutItemDto> cartItemsData = selectedItems.stream()
                .map(CheckoutItemDto::new)
                .collect(Collectors.toList());

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
        model.addAttribute("totalAmount", totalPrice); // 배송비 없으므로 subtotal과 동일
        model.addAttribute("clientKey", TOSS_CLIENT_KEY);

        return "order/checkout";
    }

    // 장바구니 주문 생성 (AJAX)
    @PostMapping("/create-from-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateOrderRequestDto requestDto) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Member member = memberService.findByEmail(userDetails.getUsername());

            // OrderService를 호출하여 DB에 주문(PENDING 상태)을 미리 생성
            Order order = orderService.createOrderFromCart(member, requestDto.getCartItemIds());

            response.put("success", true);
            response.put("orderId", order.getOrderId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("message", "주문이 성공적으로 생성되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
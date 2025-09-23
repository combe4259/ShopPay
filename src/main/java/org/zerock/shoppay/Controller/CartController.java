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
import org.zerock.shoppay.service.CartService;
import org.zerock.shoppay.service.MemberService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    private final MemberService memberService;
    
    @Value("${toss.client.key}")
    private String clientKey;
    
    // 장바구니 페이지
    @GetMapping("/cart")
    public String cart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartWithItems(member);
            model.addAttribute("cart", cart);
            model.addAttribute("totalPrice", cart.getTotalPrice());
            model.addAttribute("totalItems", cart.getTotalItems());
        }
        // 토스페이먼츠 클라이언트 키 추가
        model.addAttribute("clientKey", clientKey);
        return "cart/cart";
    }
    
    // 장바구니에 상품 추가 (AJAX)
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        //TODO: 왜 HashMap
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }
            
            Member member = memberService.findByEmail(userDetails.getUsername());
            CartItem cartItem = cartService.addToCart(member, productId, quantity);
            
            response.put("success", true);
            response.put("message", "장바구니에 추가되었습니다.");
            response.put("cartItemCount", cartService.getCartItemCount(member));
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 장바구니 아이템 수량 변경 (AJAX)
    @PostMapping("/cart/update/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }
            
            cartService.updateCartItemQuantity(cartItemId, quantity);
            
            Member member = memberService.findByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartWithItems(member);
            
            response.put("success", true);
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalItems", cart.getTotalItems());
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 장바구니 아이템 삭제 (AJAX)
    @DeleteMapping("/cart/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }
            
            cartService.removeFromCart(cartItemId);
            
            Member member = memberService.findByEmail(userDetails.getUsername());
            Cart cart = cartService.getCartWithItems(member);
            
            response.put("success", true);
            response.put("message", "장바구니에서 삭제되었습니다.");
            response.put("totalPrice", cart.getTotalPrice());
            response.put("totalItems", cart.getTotalItems());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 장바구니 비우기
    @PostMapping("/cart/clear")
    public String clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            cartService.clearCart(member);
        }
        return "redirect:/cart";
    }
    
    // 장바구니 아이템 개수 조회 (헤더용)
    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            return ResponseEntity.ok(cartService.getCartItemCount(member));
        }
        return ResponseEntity.ok(0);
    }
}
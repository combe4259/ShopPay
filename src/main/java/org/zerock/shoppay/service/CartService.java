package org.zerock.shoppay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.CartItem;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CartItemRepository;
import org.zerock.shoppay.repository.CartRepository;
import org.zerock.shoppay.repository.ProductRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    // 회원의 장바구니 조회 (없으면 생성)
    @Transactional
    public Cart getOrCreateCart(Member member) {
        return cartRepository.findByMember(member)
            .orElseGet(() -> {
                Cart newCart = Cart.builder()
                    .member(member)
                    .build();
                return cartRepository.save(newCart);
            });
    }
    
    // 장바구니에 상품 추가
    @Transactional
    public CartItem addToCart(Member member, Long productId, int quantity) {
        Cart cart = getOrCreateCart(member);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 재고 확인
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + product.getStock());
        }
        
        // 이미 장바구니에 있는 상품인지 확인
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // 이미 있으면 수량만 증가
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + product.getStock());
            }
            
            item.increaseQuantity(quantity);
            return cartItemRepository.save(item);
        } else {
            // 새로운 아이템 추가
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
            cart.addCartItem(newItem);
            return cartItemRepository.save(newItem);
        }
    }
    
    // 장바구니 아이템 수량 변경
    @Transactional
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));
        
        // 재고 확인
        if (cartItem.getProduct().getStock() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + cartItem.getProduct().getStock());
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.updateQuantity(quantity);
        }
    }
    
    // 장바구니 아이템 삭제
    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
    
    // 장바구니 비우기
    @Transactional
    public void clearCart(Member member) {
        Cart cart = getOrCreateCart(member);
        cartItemRepository.deleteAllByCart(cart);
        cart.clear();
    }
    
    // 장바구니 조회 (아이템 포함)
    public Cart getCartWithItems(Member member) {
        // 기존 Fetch Join 버전 (N+1 문제 해결)
        /*
        return cartRepository.findByMemberIdWithItems(member.getId())
            .orElseGet(() -> Cart.builder().member(member).build());
        */
        
        // N+1 문제 발생 버전 (테스트용)
        System.out.println("========== N+1 문제 테스트 시작 ==========");
        Cart cart = cartRepository.findByMemberId(member.getId())
            .orElseGet(() -> Cart.builder().member(member).build());
        
        // 이 시점에서는 Cart만 조회됨 (1번 쿼리)
        System.out.println("Cart 조회 완료 - 1번째 쿼리 실행");
        
        // getCartItems() 호출 시 LAZY 로딩으로 CartItems 조회 (2번째 쿼리)
        System.out.println("CartItems 개수: " + cart.getCartItems().size());
        
        // 각 CartItem의 Product 접근 시 추가 쿼리 발생 (N번 쿼리)
        for (CartItem item : cart.getCartItems()) {
            System.out.println("Product 조회: " + item.getProduct().getName() + " - 추가 쿼리 발생!");
        }
        
        System.out.println("========== N+1 문제 테스트 종료 ==========");
        return cart;
    }
    
    // 장바구니 아이템 개수
    public int getCartItemCount(Member member) {
        Cart cart = cartRepository.findByMember(member).orElse(null);
        return cart != null ? cart.getTotalItems() : 0;
    }
    
    // 장바구니 총 금액
    public int getCartTotalPrice(Member member) {
        Cart cart = cartRepository.findByMember(member).orElse(null);
        return cart != null ? cart.getTotalPrice() : 0;
    }
}
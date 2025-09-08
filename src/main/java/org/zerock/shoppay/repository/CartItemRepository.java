package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.CartItem;
import org.zerock.shoppay.Entity.Product;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // 장바구니에서 특정 상품 찾기
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // 장바구니 아이템 삭제
    void deleteByCartAndProduct(Cart cart, Product product);
    
    // 장바구니의 모든 아이템 삭제
    void deleteAllByCart(Cart cart);
}
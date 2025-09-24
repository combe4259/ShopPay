package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.Member;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // 회원의 장바구니 찾기
    Optional<Cart> findByMember(Member member);

    //JPQL로 변경하여 성능 비교
    // 회원 ID로 장바구니 찾기 (Fetch Join으로 CartItem과 Product 함께 로드)
    /*
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE c.member.id = :memberId")
    Optional<Cart> findByMemberIdWithItems(@Param("memberId") Long memberId);
    */
    
    // N+1 문제 발생 버전 (JPA 메서드 네이밍 사용)
    Optional<Cart> findByMemberId(Long memberId);
    
    // 회원의 장바구니 존재 여부 확인
    boolean existsByMember(Member member);
}
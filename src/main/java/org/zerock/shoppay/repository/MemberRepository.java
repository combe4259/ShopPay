package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);
    
    // 이메일 중복 체크
    boolean existsByEmail(String email);
    
    // 이메일과 활성 상태로 회원 찾기
    Optional<Member> findByEmailAndIsActive(String email, Boolean isActive);
    
    // 이름과 전화번호로 회원 찾기 (아이디 찾기용)
    Optional<Member> findByNameAndPhone(String name, String phone);
}
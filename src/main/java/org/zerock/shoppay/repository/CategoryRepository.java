package org.zerock.shoppay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 카테고리명으로 조회
    Optional<Category> findByName(String name);
    
    // 카테고리명 존재 여부 확인
    boolean existsByName(String name);
}
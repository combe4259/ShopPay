package org.zerock.shoppay.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /*
    SELECT * FROM products WHERE is_active = true
     */
    List<Product> findByIsActiveTrue();

    /*
    카테고리 ID로 상품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /*
    SELECT * FROM products WHERE name LIKE %keyword%
     */
    List<Product> findByNameContaining(String keyword);

    /*
    카테고리명으로 활성 상품 조회
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName AND p.isActive = true")
    List<Product> findByCategoryNameAndIsActiveTrue(@Param("categoryName") String categoryName);
    
    /*
    카테고리명으로 활성 상품 조회 (페이지네이션)
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName AND p.isActive = true")
    Page<Product> findByCategoryAndActiveTrue(@Param("categoryName") String categoryName, Pageable pageable);
}
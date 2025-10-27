package org.zerock.shoppay.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.exception.InsufficientStockException;
import org.zerock.shoppay.exception.OptimisticLockConflictException;
import org.zerock.shoppay.exception.ProductNotFoundException;
import org.zerock.shoppay.repository.ProductRepository;
import org.zerock.shoppay.repository.ProductSpecification;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }
    
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameAndIsActiveTrue(categoryName);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }
    
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setIsActive(false);
    }
    
    // 낙관적 락 + 재시도 로직 적용
    @Retryable(
        value = { ObjectOptimisticLockingFailureException.class, OptimisticLockException.class },
        maxAttempts = 5,
        backoff = @Backoff(delay = 50)
    )
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        System.out.println("재고 감소 시도: " + productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다: " + productId));
        //재고가 처음부터 부족한 로직 - 재시도 안됨
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }
        
        product.setStock(product.getStock() - quantity);
    }

    //재고 감소 전부 실패시 자동 호출
    @Recover
    public void recoverDecreaseStock(RuntimeException e, Long productId, Integer quantity) {
        System.err.println("최대 재시도 횟수 초과: " + productId + ", 이유: " + e.getMessage());
        throw new OptimisticLockConflictException("다른 사용자와의 충돌로 인해 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    //native SQL을 이용한 재고 감소
    @Transactional
    public void decreaseStockWithNativeQuery(Long productId, Integer quantity) {
        int updatedRows = productRepository.decreaseStockNative(productId, quantity);
        if (updatedRows == 0) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }
    }

    @Transactional
    public void decreaseStockWithPessimisticLock(Long productId, Integer quantity) {
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }

        product.setStock(product.getStock() - quantity);
    }
    
    @Transactional(readOnly = true)
    public Page<Product> findProducts(String category, Pageable pageable) {
        // 1. 기본적으로 is_active = true 조건을 설정합니다.
        Specification<Product> spec = Specification.where(ProductSpecification.isActive());

        // 2. category 파라미터가 있으면, 카테고리 필터 조건을 추가합니다.
        // ProductSpecification.hasCategory가 null 또는 빈 문자열을 안전하게 처리합니다.
        spec = spec.and(ProductSpecification.hasCategory(category));

        // 3. 최종 조합된 조건으로 Repository에 쿼리를 요청합니다.
        return productRepository.findAll(spec, pageable);
    }

    // 재고 증가 (주문 취소 시 재고 복구용)
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다: " + productId));

        product.setStock(product.getStock() + quantity);
        System.out.println("재고 복구: 상품 ID=" + productId + ", 수량=" + quantity);
    }
}

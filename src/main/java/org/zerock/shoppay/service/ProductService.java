package org.zerock.shoppay.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        //재고가 처음부터 부족한 로 - 재시도 안됨
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }
        
        product.setStock(product.getStock() - quantity);
    }

    // decreaseStock 메소드의 모든 재시도가 실패했을 때 호출될 메소드
    @Recover
    public void recoverDecreaseStock(RuntimeException e, Long productId, Integer quantity) {
        System.err.println("최대 재시도 횟수 초과: " + productId + ", 이유: " + e.getMessage());
        throw new OptimisticLockConflictException("다른 사용자와의 충돌로 인해 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    @Transactional
    public void decreaseStockWithNativeQuery(Long productId, Integer quantity) {
        int updatedRows = productRepository.decreaseStockNative(productId);
        if (updatedRows == 0) {
            throw new RuntimeException("재고가 부족하거나 상품이 존재하지 않습니다.");
        }
    }

    @Transactional
    public void decreaseStockWithPessimisticLock(Long productId, Integer quantity) {
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStock(product.getStock() - quantity);
    }
    
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryWithPagination(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryAndActiveTrue(categoryName, pageable);
    }
}
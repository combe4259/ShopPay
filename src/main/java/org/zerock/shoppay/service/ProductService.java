package org.zerock.shoppay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    // 모든 활성 상품 조회
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }
    
    // 모든 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // ID로 상품 조회
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    // 카테고리별 상품 조회
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameAndIsActiveTrue(categoryName);
    }
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    // 상품 검색
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }
    
    // 상품 저장
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    // 상품 수정
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        
        return productRepository.save(product);
    }
    
    // 상품 삭제 (soft delete)
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    // 재고 감소
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    // 네이티브 쿼리를 이용한 재고 감소
    @Transactional
    public void decreaseStockWithNativeQuery(Long productId, Integer quantity) {
        int updatedRows = productRepository.decreaseStockNative(productId);
        if (updatedRows == 0) {
            throw new RuntimeException("재고가 부족하거나 상품이 존재하지 않습니다.");
        }
    }

    // 비관적 락을 이용한 재고 감소
    @Transactional
    public void decreaseStockWithPessimisticLock(Long productId, Integer quantity) {
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("재고가 부족하거나 상품이 존재하지 않습니다.");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
    
    // 카테고리별 상품 조회 (페이지네이션)
    public Page<Product> getProductsByCategoryWithPagination(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryAndActiveTrue(categoryName, pageable);
    }
}
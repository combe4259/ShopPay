package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    // 상품 목록 페이지
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "product/list";
    }
    
    // 상품 상세 페이지
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product/detail";
    }
    
//    카테고리별 상품 목록 (ID로 조회)
//    @GetMapping("/category/{categoryId}")
//    public String productsByCategory(@PathVariable Long categoryId, Model model) {
//        List<Product> products = productService.getProductsByCategoryId(categoryId);
//        model.addAttribute("products", products);
//        model.addAttribute("categoryId", categoryId);
//        return "product/list";
//    }
    @GetMapping("/category/{categoryName}")
    public String productsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {
        
        // 정렬 방향 설정
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // 페이지네이션된 상품 조회
        Page<Product> productPage = productService.getProductsByCategoryWithPagination(categoryName, pageable);
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("category", categoryName);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        
        // 페이지 번호 목록 생성 (현재 페이지 기준 앞뒤 5개)
        int startPage = Math.max(0, page - 5);
        int endPage = Math.min(productPage.getTotalPages() - 1, page + 5);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        
        return "product/category";  // IKEA 스타일 카테고리 페이지
    }
    
    // 상품 검색
    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "home/index";
    }
    

}
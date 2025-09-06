package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
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
    public String productsByCategory(@PathVariable String categoryName,Model model) {
        List<Product> products = productService.getProductsByCategory(categoryName);
        model.addAttribute("products", products);
        model.addAttribute("category", categoryName);
        return "product/category";  // IKEA 스타일 카테고리 페이지
    }
    
    // 상품 검색
    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "product/list";
    }
    

}
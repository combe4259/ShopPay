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
    

    // 상품 상세 페이지
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product/detail";
    }
    
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        // 정렬 방향 설정
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // 통합된 서비스 메서드 호출
        Page<Product> productPage = productService.findProducts(category, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        // category가 null일 경우 'All'을, 아니면 해당 카테고리 이름을 모델에 추가
        model.addAttribute("category", category != null ? category : "All");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        // 페이지 번호 목록 생성
        int startPage = Math.max(0, page - 5);
        int endPage = Math.min(productPage.getTotalPages() - 1, page + 5);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "product/category";  // 템플릿 재사용
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
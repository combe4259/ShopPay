//package org.zerock.shoppay.Controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.zerock.shoppay.Entity.Category;
//import org.zerock.shoppay.repository.CategoryRepository;
//import org.zerock.shoppay.service.ProductService;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/admin")
//public class AdminController {
//
//    private final ProductService productService;
//    private final CategoryRepository categoryRepository;
//
//    @GetMapping("/categories")
//    public String categoryList(Model model) {
//        model.addAttribute("categories", categoryRepository.findAll());
//        return "admin/categories";
//    }
//
//    @PostMapping("/categories")
//    @ResponseBody
//    public String addCategory(Category category) {
//        if(categoryRepository.existsByName(category.getName())) {
//            return "존재하는 카테고리";
//        }
//        categoryRepository.save(category);
//        return "카테고리 추가";
//    }
//    //    // 관리자용: 상품 추가 폼
////    public String newProductForm(Model model) {
////        model.addAttribute("product", new Product());
////        return "product/form";
////    }
////
////    // 관리자용: 상품 추가 처리
////    @GetMapping("/admin/new")
////    @PostMapping("/admin/new")
////    public String createProduct(@ModelAttribute Product product) {
////        productService.saveProduct(product);
////        return "redirect:/products";
////    }
////
////    // 관리자용: 상품 수정 폼
////    @GetMapping("/admin/edit/{id}")
////    public String editProductForm(@PathVariable Long id, Model model) {
////        Product product = productService.getProductById(id)
////                .orElseThrow(() -> new RuntimeException("Product not found"));
////        model.addAttribute("product", product);
////        return "product/form";
////    }
////
////    // 관리자용: 상품 수정 처리
////    @PostMapping("/admin/edit/{id}")
////    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
////        productService.updateProduct(id, product);
////        return "redirect:/products/" + id;
////    }
////
////    // 관리자용: 상품 삭제
////    @PostMapping("/admin/delete/{id}")
////    public String deleteProduct(@PathVariable Long id) {
////        productService.deleteProduct(id);
////        return "redirect:/products";
////    }
//}

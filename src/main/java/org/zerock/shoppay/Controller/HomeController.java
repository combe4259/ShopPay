package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.service.ProductService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String Home(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("clientKey", "${toss.client.key}");
        return "home/index";
    }

}

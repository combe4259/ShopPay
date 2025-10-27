package org.zerock.shoppay.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.repository.ProductRepository;
import org.zerock.shoppay.util.ImprovedDataGenerator;

//@Component  // 임시로 비활성화
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImprovedDataGenerator improvedDataGenerator;
    
    @Override
    public void run(String... args) throws Exception {
        // 카테고리 먼저 초기화
        if (categoryRepository.count() == 0) {
            Category bed = Category.builder().name("Bed").build();
            Category kitchen = Category.builder().name("Kitchen").build();
            Category living = Category.builder().name("Living").build();
            Category newCategory = Category.builder().name("new").build();
            Category chair = Category.builder().name("chair").build();
            Category storeFurniture = Category.builder().name("store_furniture").build();
            Category stored = Category.builder().name("stored").build();
            Category deskChair = Category.builder().name("desk_chair").build();
            Category kitchinChair = Category.builder().name("kitchin_chair").build();
            Category outdoor = Category.builder().name("outdoor").build();
            Category plant = Category.builder().name("plant").build();
            Category deco = Category.builder().name("deco").build();
            Category lightning = Category.builder().name("lightning").build();

            categoryRepository.save(bed);
            categoryRepository.save(kitchen);
            categoryRepository.save(living);
            categoryRepository.save(newCategory);
            categoryRepository.save(chair);
            categoryRepository.save(storeFurniture);
            categoryRepository.save(stored);
            categoryRepository.save(deskChair);
            categoryRepository.save(kitchinChair);
            categoryRepository.save(outdoor);
            categoryRepository.save(plant);
            categoryRepository.save(deco);
            categoryRepository.save(lightning);
            
            System.out.println("카테고리 13개가 추가되었습니다!");
        }
        
        // 그 다음 상품 초기화
        if (productRepository.count() == 0) {
            System.out.println("향상된 데이터 생성기를 사용하여 50개의 상품을 생성합니다...");
            improvedDataGenerator.generateProducts(50);
            System.out.println("상품 50개 생성이 완료되었습니다!");
        }
    }
}

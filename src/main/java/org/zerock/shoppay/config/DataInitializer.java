package org.zerock.shoppay.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.repository.ProductRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 카테고리 먼저 초기화
        if (categoryRepository.count() == 0) {
            Category bed = Category.builder().name("Bed").build();
            Category kitchen = Category.builder().name("Kitchen").build();
            Category living = Category.builder().name("Living").build();
            
            // 카테고리 먼저 저장!
            bed = categoryRepository.save(bed);
            kitchen = categoryRepository.save(kitchen);
            living = categoryRepository.save(living);
            
            System.out.println("카테고리 3개가 추가되었습니다!");
        }
        
        // 그 다음 상품 초기화
        if (productRepository.count() == 0) {
            // 저장된 카테고리 조회
            Category bed = categoryRepository.findByName("Bed").orElse(null);
            Category kitchen = categoryRepository.findByName("Kitchen").orElse(null);
            Category living = categoryRepository.findByName("Living").orElse(null);
            
            // IKEA 상품 추가
            Product ikea1 = Product.builder()
                    .name("NÅLBLECKA 놀블레카")
                    .description("주방 조리대 정리용품, 38x13x28 cm")
                    .price(19900)
                    .stock(20)
                    .category(kitchen)
                    .imageUrl("https://www.ikea.com/kr/ko/images/products/nalblecka-spice-rack-bamboo__1152034_pe884774_s5.jpg")
                    .isActive(true)
                    .build();
            
            Product ikea2 = Product.builder()
                    .name("VEVELSTAD 베벨스타드")
                    .description("침대프레임, 화이트, 120x200 cm")
                    .price(119000)
                    .stock(5)
                    .category(bed)
                    .imageUrl("https://www.ikea.com/kr/ko/images/products/vevelstad-bed-frame-white__0749531_pe745501_s5.jpg")
                    .isActive(true)
                    .build();
            
            Product ikea3 = Product.builder()
                    .name("SMASKA 스마스카")
                    .description("도시락통, 플라스틱/그린")
                    .price(3900)
                    .stock(50)
                    .category(kitchen)
                    .imageUrl("https://www.ikea.com/kr/ko/images/products/smaska-lunch-box-green__1153271_pe885950_s5.jpg")
                    .isActive(true)
                    .build();
            
            Product ikea4 = Product.builder()
                    .name("VÄGSKYLT 벡쉴트")
                    .description("평직러그, 80x150 cm")
                    .price(34900)
                    .stock(8)
                    .category(living)
                    .imageUrl("https://www.ikea.com/kr/ko/images/products/vagskylt-rug-flatwoven-multicolour__1117853_pe872668_s5.jpg")
                    .isActive(true)
                    .build();
            
            Product ikea5 = Product.builder()
                    .name("KUNGSFORS 쿵스포르스")
                    .description("주방카트, 스테인리스/애쉬, 60x40 cm")
                    .price(149000)
                    .stock(3)
                    .category(kitchen)
                    .imageUrl("https://www.ikea.com/kr/ko/images/products/kungsfors-kitchen-trolley-stainless-steel-ash__0714236_pe730090_s5.jpg")
                    .isActive(true)
                    .build();
            
            productRepository.save(ikea1);
            productRepository.save(ikea2);
            productRepository.save(ikea3);
            productRepository.save(ikea4);
            productRepository.save(ikea5);
            
            System.out.println("초기 상품 데이터 10개가 추가되었습니다! (일반 5개 + IKEA 5개)");
        }
    }
}
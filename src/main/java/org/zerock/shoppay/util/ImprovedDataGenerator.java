package org.zerock.shoppay.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.repository.ProductRepository;

import java.util.*;

@Component
@RequiredArgsConstructor
@Profile("dev") // 이 생성기는 'dev' 프로파일에서만 활성화됩니다.
public class ImprovedDataGenerator {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnsplashImageService unsplashImageService;
    private final Random random = new Random();

    // 제품 생성을 위한 템플릿 구조체
    private static class ProductTemplate {
        final String swedishName;
        final String koreanName;
        final String descriptionTemplate;
        final int minPrice;
        final int maxPrice;

        ProductTemplate(String swedishName, String koreanName, String description, int minPrice, int maxPrice) {
            this.swedishName = swedishName;
            this.koreanName = koreanName;
            this.descriptionTemplate = description;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }

    // 카테고리별 제품 템플릿 데이터 풀
    private final Map<String, List<ProductTemplate>> productDataPool = Map.of(
        "Bed", List.of(
            new ProductTemplate("MALM", "말름", "%s 색상의 높은 침대프레임, %dx200 cm", 200000, 500000),
            new ProductTemplate("HEMNES", "헴네스", "%s 스테인 처리된 데이베드프레임, 서랍 3개", 250000, 600000)
        ),
        "Kitchen", List.of(
            new ProductTemplate("METOD", "메토드", "%s 도어의 주방 벽수납장, %dx%d cm", 100000, 400000),
            new ProductTemplate("KUNGSFORS", "쿵스포르스", "%s 재질의 주방 트롤리", 50000, 200000)
        ),
        "Living", List.of(
            new ProductTemplate("EKTORP", "엑토르프", "%s 색상의 %d인용 패브릭 소파", 300000, 800000),
            new ProductTemplate("KIVIK", "시비크", "%s 색상의 코너소파, 5인용", 400000, 900000)
        ),
        "chair", List.of(
            new ProductTemplate("INGOLF", "잉올프", "%s 색상의 %s 의자", 50000, 150000),
            new ProductTemplate("ADDE", "아데", "심플한 디자인의 %s 식탁 의자", 20000, 50000)
        )
    );

    private final String[] COLORS = {"화이트", "블랙", "그레이", "베이지", "브라운", "네이비", "그린", "레드"};
    private final String[] MATERIALS = {"참나무", "자작나무", "대나무", "파티클보드", "스틸", "패브릭", "가죽"};

    // DataInitializer에서 호출될 메서드
    public void generateProducts(int totalProductsToCreate) {
        long currentCount = productRepository.count();
        if (currentCount > 0) {
            System.out.println("이미 상품 데이터가 " + currentCount + "개 존재하여, 추가 데이터를 생성하지 않습니다.");
            return;
        }

        System.out.println("=== 향상된 데이터 생성 시작 (목표: " + totalProductsToCreate + "개) ===");
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            System.out.println("오류: 카테고리가 존재하지 않아 상품을 생성할 수 없습니다.");
            return;
        }

        List<Product> productsToSave = new ArrayList<>();
        int productsPerCategory = totalProductsToCreate / categories.size();
        int remainder = totalProductsToCreate % categories.size();

        for (Category category : categories) {
            int productsToCreateForThisCategory = productsPerCategory + (remainder-- > 0 ? 1 : 0);
            if (productsToCreateForThisCategory == 0) continue;

            System.out.println("카테고리 '" + category.getName() + "'에 대한 이미지 " + productsToCreateForThisCategory + "개를 가져옵니다...");
            List<String> imageUrls = unsplashImageService.getRandomImageUrls(category.getName(), productsToCreateForThisCategory);
            
            for (int i = 0; i < productsToCreateForThisCategory; i++) {
                String imageUrl = imageUrls.get(i % imageUrls.size()); // 받아온 이미지 목록 내에서 순환 사용
                Product product = generateRandomProductForCategory(category, imageUrl);
                productsToSave.add(product);
            }
        }

        productRepository.saveAll(productsToSave);
        System.out.println("=== 데이터 생성 완료: 총 " + productRepository.count() + "개 상품 생성됨 ===");
    }

    private Product generateRandomProductForCategory(Category category, String imageUrl) {
        List<ProductTemplate> templates = productDataPool.getOrDefault(category.getName(), 
            List.of(new ProductTemplate("GENERIC", "기본", "%s 색상의 %s 제품", 10000, 50000)));
        
        ProductTemplate template = templates.get(random.nextInt(templates.size()));

        // 이름은 스웨덴어 + 한글 이름으로 간결하게 생성
        String name = String.format("%s %s", template.swedishName, template.koreanName);

        // 설명은 '재질' 정보를 맨 앞에 추가하여 생성
        String originalDescription = String.format(template.descriptionTemplate.replace("%d", "%s"),
                COLORS[random.nextInt(COLORS.length)],
                String.valueOf(60 + random.nextInt(5) * 20),
                String.valueOf(80 + random.nextInt(5) * 20));
        
        String description = MATERIALS[random.nextInt(MATERIALS.length)] + " 재질, " + originalDescription;

        int price = template.minPrice + random.nextInt(template.maxPrice - template.minPrice);
        price = (price / 1000) * 1000 + 900;

        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(random.nextInt(100) + 1)
                .imageUrl(imageUrl)
                .category(category)
                .isActive(true)
                .build();
    }
}
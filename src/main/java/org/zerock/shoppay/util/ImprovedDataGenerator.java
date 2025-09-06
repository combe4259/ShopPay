package org.zerock.shoppay.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class ImprovedDataGenerator {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Random random = new Random();
    
    // 카테고리 Enum으로 관리
    private enum CategoryType {
        BED("Bed", "침대"),
        KITCHEN("Kitchen", "주방"),
        LIVING("Living", "거실"),
        CHAIR("chair", "의자"),
        DESK_CHAIR("desk_chair", "사무용 의자"),
        KITCHEN_CHAIR("kitchin_chair", "주방 의자"),
        OUTDOOR("outdoor", "야외"),
        PLANT("plant", "식물"),
        STORAGE("store_furniture", "수납"),
        STORED("stored", "보관"),
        DECO("deco", "장식"),
        LIGHTING("lightning", "조명"),
        NEW("new", "신제품");
        
        private final String dbName;
        private final String koreanName;
        
        CategoryType(String dbName, String koreanName) {
            this.dbName = dbName;
            this.koreanName = koreanName;
        }
        
        public static CategoryType fromDbName(String dbName) {
            for (CategoryType type : values()) {
                if (type.dbName.equalsIgnoreCase(dbName)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    // 제품 데이터 구조체
    private static class ProductData {
        final String swedishName;
        final String koreanName;
        final List<String> descriptionTemplates;
        final int minPrice;
        final int maxPrice;
        final List<String> imageUrls;
        
        ProductData(String swedishName, String koreanName, List<String> descriptions, 
                   int minPrice, int maxPrice, List<String> imageUrls) {
            this.swedishName = swedishName;
            this.koreanName = koreanName;
            this.descriptionTemplates = descriptions;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.imageUrls = imageUrls;
        }
    }
    
    // 카테고리별 제품 데이터 풀
    private final Map<CategoryType, List<ProductData>> productDataPool = new HashMap<>() {{
        // 침대 카테고리
        put(CategoryType.BED, Arrays.asList(
            new ProductData("MALM", "말름", 
                Arrays.asList(
                    "침대프레임, 높은형, %s, %dx200 cm",
                    "수납침대, %d개 서랍, %s 마감",
                    "침대프레임+헤드보드, %s"
                ),
                200000, 500000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1540835296355-c04f7a063cbb?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=400&h=400&fit=crop"
                )
            ),
            new ProductData("HEMNES", "헴네스",
                Arrays.asList(
                    "데이베드프레임, 서랍 3개, %s",
                    "침대프레임, %s 스테인, %dx200 cm",
                    "침대프레임+수납상자 4개, %s"
                ),
                250000, 600000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1540835296355-c04f7a063cbb?w=400&h=400&fit=crop"
                )
            ),
            new ProductData("BRIMNES", "브림네스",
                Arrays.asList(
                    "침대프레임+헤드보드, %s, %dx200 cm",
                    "데이베드프레임, 서랍 2개, %s",
                    "수납침대, %s 마감"
                ),
                180000, 450000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=400&h=400&fit=crop"
                )
            )
        ));
        
        // 주방 카테고리
        put(CategoryType.KITCHEN, Arrays.asList(
            new ProductData("METOD", "메토드",
                Arrays.asList(
                    "주방수납장, %s, %dx%d cm",
                    "벽수납장, %s 도어",
                    "하부장, %s/%s"
                ),
                100000, 400000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1565538810643-b5bdb714032a?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1556911220-bff31c812dba?w=400&h=400&fit=crop"
                )
            ),
            new ProductData("KUNGSFORS", "쿵스포르스",
                Arrays.asList(
                    "주방 선반유닛, %s",
                    "주방 트롤리, %s",
                    "벽선반, %s/%s"
                ),
                50000, 200000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop"
                )
            )
        ));
        
        // 거실 카테고리
        put(CategoryType.LIVING, Arrays.asList(
            new ProductData("EKTORP", "엑토르프",
                Arrays.asList(
                    "%d인용 소파, %s",
                    "코너소파, 4인용, %s",
                    "소파베드, %s"
                ),
                300000, 800000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1512212621149-107ffe572d2f?w=400&h=400&fit=crop"
                )
            ),
            new ProductData("KIVIK", "시비크",
                Arrays.asList(
                    "%d인용 소파, %s",
                    "코너소파, 5인용, %s/%s",
                    "긴의자섹션, %s"
                ),
                400000, 900000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=400&h=400&fit=crop"
                )
            )
        ));
        
        // 의자 카테고리
        put(CategoryType.CHAIR, Arrays.asList(
            new ProductData("INGOLF", "잉올프",
                Arrays.asList(
                    "의자, %s",
                    "바 스툴, 등받이, %dcm, %s",
                    "유아용 의자, %s"
                ),
                50000, 150000,
                Arrays.asList(
                    "https://images.unsplash.com/photo-1592078615290-033ee584e267?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1549497538-303791108f95?w=400&h=400&fit=crop",
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=400&h=400&fit=crop"
                )
            )
        ));
    }};
    
    private final String[] COLORS = {"화이트", "블랙", "그레이", "베이지", "브라운", "네이비", "그린", "레드"};
    private final String[] MATERIALS = {"참나무", "자작나무", "대나무", "파티클보드", "스틸", "패브릭", "가죽"};
    
    @EventListener(ApplicationReadyEvent.class)
    public void generateBulkData() {
        int targetCount = 500;
        long currentCount = productRepository.count();
        
        if (currentCount >= targetCount) {
            System.out.println("이미 충분한 데이터가 존재합니다: " + currentCount + "개");
            return;
        }
        
        System.out.println("=== 데이터 생성 시작 ===");
        System.out.println("현재: " + currentCount + "개 → 목표: " + targetCount + "개");
        
        List<Category> categories = categoryRepository.findAll();
        List<Product> productsToSave = new ArrayList<>();
        
        // 카테고리별로 제품 생성
        for (Category category : categories) {
            CategoryType categoryType = CategoryType.fromDbName(category.getName());
            if (categoryType == null) continue;
            
            List<ProductData> dataPool = productDataPool.get(categoryType);
            if (dataPool == null || dataPool.isEmpty()) {
                dataPool = getDefaultProductData(); // 기본 데이터 사용
            }
            
            // 각 카테고리당 생성할 제품 수
            int productsPerCategory = (targetCount - (int)currentCount) / categories.size();
            
            for (int i = 0; i < productsPerCategory; i++) {
                Product product = generateProductFromData(category, dataPool);
                productsToSave.add(product);
                
                // 배치 사이즈 도달 시 저장 (메모리 효율성)
                if (productsToSave.size() >= 50) {
                    productRepository.saveAll(productsToSave);
                    productsToSave.clear();
                    System.out.println("... " + productRepository.count() + "개 생성 완료");
                }
            }
        }
        
        // 남은 제품 저장
        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);
        }
        
        System.out.println("=== 생성 완료: 총 " + productRepository.count() + "개 ===");
    }
    
    private Product generateProductFromData(Category category, List<ProductData> dataPool) {
        ProductData data = dataPool.get(random.nextInt(dataPool.size()));
        
        // 이름 생성 (번호 추가로 중복 방지)
        String name = String.format("%s %s #%d", 
            data.swedishName, data.koreanName, System.currentTimeMillis() % 10000);
        
        // 설명 생성 (템플릿 활용)
        String description = generateDescription(data.descriptionTemplates);
        
        // 가격 생성 (900원 단위)
        int price = data.minPrice + random.nextInt(data.maxPrice - data.minPrice);
        price = (price / 1000) * 1000 + 900;
        
        // 이미지 선택
        String imageUrl = data.imageUrls.get(random.nextInt(data.imageUrls.size()));
        
        return Product.builder()
            .name(name)
            .description(description)
            .price(price)
            .stock(random.nextInt(50) + 5)
            .imageUrl(imageUrl)
            .category(category)
            .isActive(true)
            .build();
    }
    
    private String generateDescription(List<String> templates) {
        String template = templates.get(random.nextInt(templates.size()));
        
        // 템플릿의 %s, %d 치환
        template = template.replaceFirst("%s", COLORS[random.nextInt(COLORS.length)]);
        template = template.replaceFirst("%s", MATERIALS[random.nextInt(MATERIALS.length)]);
        template = template.replaceFirst("%d", String.valueOf(2 + random.nextInt(4)));
        template = template.replaceFirst("%d", String.valueOf(60 + random.nextInt(3) * 20));
        
        return template;
    }
    
    private List<ProductData> getDefaultProductData() {
        // 기본 제품 데이터 (카테고리 매핑이 없을 때)
        return Arrays.asList(
            new ProductData("LACK", "락",
                Arrays.asList("선반유닛, %s", "벽선반, %s"),
                30000, 100000,
                Arrays.asList("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=400&fit=crop")
            )
        );
    }
}
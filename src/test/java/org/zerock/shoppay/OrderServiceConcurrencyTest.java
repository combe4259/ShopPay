package org.zerock.shoppay;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Category;
import org.zerock.shoppay.Entity.Product;
import org.zerock.shoppay.repository.CategoryRepository;
import org.zerock.shoppay.repository.ProductRepository;
import org.zerock.shoppay.service.OrderService;
import org.zerock.shoppay.service.ProductService;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;

    private Long testProductId;

    @AfterEach
    public void tearDown() {
        if (testProductId != null) {
            productRepository.deleteById(testProductId);
        }
    }


    @Test
    void concurrencyTest() throws InterruptedException {

        Optional<Category> category = categoryRepository.findByName("Living");
        Category living = category.orElseThrow(() -> new RuntimeException("Living Category Not Found"));
        //Given 재고가 1개인 상품
        Product newProduct = Product.builder()
                .name("forTest")
                .price(10000)
                .description("test")
                .stock(1)
                .category(living)
                .isActive(true)
                .build();
        Product savedProduct = productRepository.saveAndFlush(newProduct);
        this.testProductId = savedProduct.getId();
        Long productId = savedProduct.getId();

        //When 2명의 사용자가 동시에 1개씩 주문
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    //productService.decreaseStock(productId, 1);
                    //productService.decreaseStockWithNativeQuery(productId,1);
                    //productService.decreaseStockWithPessimisticLock(productId,1);
                    productService.decreaseStock(productId, 1);
                }catch (RuntimeException e){
                    System.out.println("재고 부족 예외"+e.getMessage());
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product finalProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(0, finalProduct.getStock());

    }

}

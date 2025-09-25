package org.zerock.shoppay.Controller.JmeterTest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.shoppay.service.ProductService;

/*
재고 감소 테스트 JMeter
 */
@RestController
@RequestMapping("/test/stock")
@RequiredArgsConstructor
public class StockTestConroller {

    private final ProductService productService;

    @PostMapping("/decrease-native/{productId}")
    public ResponseEntity<String> decreaseStockNative(@PathVariable("productId") Long productId) {
        productService.decreaseStockWithNativeQuery(productId, 1);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/decrease-pessimistic/{productId}")
    public ResponseEntity<String> decreaseStockPessimistic(@PathVariable("productId") Long productId) {
        productService.decreaseStockWithPessimisticLock(productId, 1);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/decrease-optimistic/{productId}")
    public ResponseEntity<String> decreaseStockOptimistic(@PathVariable("productId") Long productId) {
        productService.decreaseStock(productId, 1);
        return ResponseEntity.ok("success");
    }



}

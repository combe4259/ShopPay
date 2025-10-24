package org.zerock.shoppay.dto;

import lombok.Getter;
import org.zerock.shoppay.Entity.Product;

@Getter
public class ProductDto {
    private final Long id;
    private final String name;
    private final int price;

    public ProductDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
    }
}

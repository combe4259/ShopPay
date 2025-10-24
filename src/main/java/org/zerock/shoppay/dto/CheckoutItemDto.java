package org.zerock.shoppay.dto;

import lombok.Getter;
import org.zerock.shoppay.Entity.CartItem;

@Getter
public class CheckoutItemDto {
    private final Long id;
    private final int quantity;
    private final int totalPrice;
    private final ProductDto product;

    public CheckoutItemDto(CartItem cartItem) {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.totalPrice = cartItem.getTotalPrice();
        this.product = new ProductDto(cartItem.getProduct());
    }
}

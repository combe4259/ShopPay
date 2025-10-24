package org.zerock.shoppay.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequestDto {
    private List<Long> cartItemIds;
}

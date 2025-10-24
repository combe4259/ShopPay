package org.zerock.shoppay.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.zerock.shoppay.service.CartService;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;

    @ModelAttribute("cartItemCount")
    public int cartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return 0; // 비로그인 사용자는 장바구니 아이템 개수가 0
        }
        return cartService.getCartItemCount(userDetails.getUsername());
    }
}

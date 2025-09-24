package org.zerock.shoppay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 결제 관련 경로
        registry.addViewController("/payment/success.html").setViewName("payment/success");
        registry.addViewController("/payment/success").setViewName("payment/success");
        registry.addViewController("/payment/fail.html").setViewName("payment/fail");
        registry.addViewController("/payment/fail").setViewName("payment/fail");
        
        // 홈 경로
        registry.addViewController("/home/index.html").setViewName("home/index");

        registry.addViewController("/order/checkout.html").setViewName("order/checkout");

        // 제품 관련 경로 (필요한 경우)
        //registry.addViewController("/product/list.html").setViewName("product/list");
        registry.addViewController("/product/detail.html").setViewName("product/detail");
        registry.addViewController("/product/category.html").setViewName("product/category");
        
        // 회원 관련 경로 (필요한 경우)
        registry.addViewController("/member/login.html").setViewName("member/login");
        registry.addViewController("/member/signup.html").setViewName("member/signup");
        registry.addViewController("/member/mypage.html").setViewName("member/mypage");
    }
}
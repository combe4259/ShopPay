package org.zerock.shoppay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // .html 확장자로 요청이 오면 templates 폴더에서 찾도록 설정
        registry.addViewController("/payment/success.html").setViewName("payment/success");
        registry.addViewController("/payment/fail.html").setViewName("payment/fail");
        registry.addViewController("/home/index.html").setViewName("home/index");
    }
}
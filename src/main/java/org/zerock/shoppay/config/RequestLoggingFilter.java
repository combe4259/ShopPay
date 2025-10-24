package org.zerock.shoppay.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1) // 모든 필터 중에서 가장 먼저 실행되도록 순서를 1로 지정
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 요청이 서버에 도착한 바로 그 순간의 URI를 로그로 출력합니다.
        log.info(">>>>> INCOMING REQUEST URI: " + httpRequest.getRequestURI());
        
        // 다음 필터로 요청을 전달합니다.
        chain.doFilter(request, response);
    }
}

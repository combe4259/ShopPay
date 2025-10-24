package org.zerock.shoppay.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zerock.shoppay.dto.ErrorResponse;
import org.zerock.shoppay.exception.InsufficientStockException;
import org.zerock.shoppay.exception.OptimisticLockConflictException;
import org.zerock.shoppay.exception.ProductNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException e) {
        log.warn(e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException e) {
        log.warn(e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockConflict(OptimisticLockConflictException e) {
        log.warn(e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // favicon.ico, .well-known 등 정적 리소스를 찾지 못하는 경우를 위한 핸들러
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.debug("Resource not found: {}", e.getMessage()); // 로그 레벨을 DEBUG로 낮춰서 불필요한 경고 방지
        ErrorResponse errorResponse = new ErrorResponse("The requested resource was not found.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    //Global Catch-All handler로 포괄적인 예외 처리를 진행
    //GlobalExceptionHandler에서 명시되지 않는 예외 이외의 모든 예외는 해당 메서드를 통해 처리 - Exception.class가 모든 예외의 최위 클래스
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        // 중요한 정보: 실제 운영 환경에서는 전체 예외 스택 트레이스를 로깅하여 디버깅에 사용해야 합니다.
        log.error("Unhandled exception occurred", e);
        ErrorResponse errorResponse = new ErrorResponse("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

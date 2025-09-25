package org.zerock.shoppay.dto;

// Java 14 이상에서 사용 가능한 record를 사용하여 불변 DTO를 간결하게 정의합니다.
public record ErrorResponse(String message) {
}

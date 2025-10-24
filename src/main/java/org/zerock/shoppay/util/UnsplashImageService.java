package org.zerock.shoppay.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnsplashImageService {

    private final WebClient webClient;
    private final String accessKey;

    // 생성자를 통해 application.properties에서 키를 주입받습니다.
    public UnsplashImageService(@Value("${unsplash.api.access-key}") String accessKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.unsplash.com")
                .build();
        this.accessKey = accessKey;
    }

    public String getImageUrl(String query) {
        try {
            // WebClient를 사용한 비동기 API 호출
            Mono<JsonNode> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/photos/random")
                            .queryParam("query", query)
                            .queryParam("client_id", accessKey)
                            .queryParam("orientation", "squarish") // 'square'보다는 'squarish'가 더 많은 결과를 줌
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class);

            // Mono에서 결과를 동기적으로 추출 (DataInitializer는 비동기일 필요 없음)
            JsonNode root = responseMono.block();

            if (root != null) {
                JsonNode urls = root.path("urls");
                // regular 사이즈 이미지 URL 반환 (없으면 small)
                if (urls.has("regular")) {
                    return urls.get("regular").asText();
                } else if (urls.has("small")) {
                    return urls.get("small").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Unsplash API 오류: " + e.getMessage() + ". 백업 이미지를 사용합니다.");
            // API 호출 실패 시, 카테고리별 백업 이미지 사용
            return getFallbackImageUrl(query);
        }

        // 모든 시도 실패 시 최종 placeholder 반환
        return String.format("https://via.placeholder.com/400x400/f0f0f0/333?text=%s", query);
    }

    public List<String> getRandomImageUrls(String query, int count) {
        List<String> imageUrls = new ArrayList<>();
        try {
            Mono<JsonNode> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/photos/random")
                            .queryParam("query", query)
                            .queryParam("count", count)
                            .queryParam("client_id", accessKey)
                            .queryParam("orientation", "squarish")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class);

            JsonNode root = responseMono.block();

            if (root != null && root.isArray()) {
                for (JsonNode photoNode : root) {
                    JsonNode urlsNode = photoNode.path("urls");
                    if (urlsNode.has("regular")) {
                        imageUrls.add(urlsNode.get("regular").asText());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Unsplash API (bulk) 오류: " + e.getMessage() + ". 백업 이미지를 사용합니다.");
        }

        // API가 실패하거나 충분한 이미지를 반환하지 못하면, 백업 이미지로 채웁니다.
        int remaining = count - imageUrls.size();
        if (remaining > 0) {
            for (int i = 0; i < remaining; i++) {
                imageUrls.add(getFallbackImageUrl(query));
            }
        }
        return imageUrls;
    }

    // 카테고리별 미리 정의된 이미지 URL (백업용)
    private String getFallbackImageUrl(String category) {
        switch (category.toLowerCase()) {
            case "bed":
                return "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=400&h=400&fit=crop";
            case "kitchen":
                return "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop";
            case "living":
                return "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400&h=400&fit=crop";
            case "chair":
                return "https://images.unsplash.com/photo-1592078615290-033ee584e267?w=400&h=400&fit=crop";
            case "plant":
                return "https://images.unsplash.com/photo-1493957988430-a5f2e15f39a3?w=400&h=400&fit=crop";
            default:
                return "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=400&h=400&fit=crop";
        }
    }
}
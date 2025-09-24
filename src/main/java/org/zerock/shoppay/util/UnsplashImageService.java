package org.zerock.shoppay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class UnsplashImageService {
    
    private static final String UNSPLASH_API_URL = "https://api.unsplash.com/photos/random";
    private static final String ACCESS_KEY = "lj2FDyGQV5ahGrq4vMkMJ1mPzmiCxFZkZluUcxKQJc0";
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    public String getImageUrl(String query) {
        try {
            // Unsplash API 호출
            String url = String.format("%s?query=%s&client_id=%s&orientation=square", 
                UNSPLASH_API_URL, query, ACCESS_KEY);
            
            String response = restTemplate.getForObject(url, String.class);
            
            // JSON 파싱
            JsonNode root = objectMapper.readTree(response);
            JsonNode urls = root.path("urls");
            
            // regular 사이즈 이미지 URL 반환 (없으면 small)
            if (urls.has("regular")) {
                return urls.get("regular").asText();
            } else if (urls.has("small")) {
                return urls.get("small").asText();
            }
            
        } catch (Exception e) {
            System.err.println("Unsplash API 오류: " + e.getMessage());
        }
        
        // 실패시 placeholder 반환
        return String.format("https://via.placeholder.com/400x400/f0f0f0/333?text=%s", query);
    }
    
    // 카테고리별 미리 정의된 이미지 URL (백업용)
    public String getFallbackImageUrl(String category) {
        switch(category.toLowerCase()) {
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
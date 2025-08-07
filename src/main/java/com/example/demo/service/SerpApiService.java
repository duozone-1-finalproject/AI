package com.example.demo.service;

import com.example.demo.dto.SerpApiSearchResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SerpApiService {

    @Value("${serpapi.api-key}")
    private String serpApiKey;

    private final WebClient.Builder webClientBuilder;

    private static final Logger log = LoggerFactory.getLogger(SerpApiService.class);
    private static final String SERPAPI_BASE_URL = "https://serpapi.com/search";

    public SerpApiSearchResponse search(String query, String engine) {
        // UriComponentsBuilder를 사용하여 URL을 동적으로 안전하게 구성합니다.
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(SERPAPI_BASE_URL)
                .queryParam("engine", engine)
                .queryParam("q", query)
                .queryParam("api_key", serpApiKey)
                .queryParam("hl", "ko")
                .queryParam("gl", "kr")
                .queryParam("num", 10);

        // 'sort_by=date' 파라미터는 google_news 엔진에서만 추가합니다.
        if ("google_news".equals(engine)) {
            builder.queryParam("sort_by", "date");
        }

        String url = builder.toUriString();

        // 디버깅을 위해 실제 요청 URL을 로그로 남깁니다.
        log.info("SerpAPI 요청 URL: {}", url);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(SerpApiSearchResponse.class)
                .block(); // 동기식으로 결과를 기다림
    }

    /**
     * LLM이 판단한 검색 유형(type)에 따라 적절한 SerpAPI 엔진 이름을 반환합니다.
     * @param type 검색 유형 (e.g., "news", "general", "report")
     * @return SerpAPI 엔진 이름 (e.g., "google_news", "google", "naver")
     */
    public String getEngineForType(String type) {
        // LLM이 유효한 카테고리를 반환하지 못해 type이 null인 경우를 대비한 방어 코드
        if (type == null || type.isBlank()) {
            log.warn("LLM으로부터 유효한 검색 유형을 받지 못했습니다. 기본 엔진 'google'을 사용합니다.");
            return "google"; // 안전하게 기본값 반환
        }
        return switch (type.toLowerCase().trim()) { // trim()을 추가하여 만약의 공백에 대비
            case "news" -> "google_news";
            case "general" -> "google";
            case "report" -> "naver";
            default -> "google"; // 매핑되는 타입이 없을 경우 기본값
        };
    }
}



    

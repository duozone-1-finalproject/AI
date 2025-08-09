package com.example.demo.service;

import com.example.demo.dto.SerpApiSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.MediaType;

@Service
public class SerpApiServiceBackUp {

    @Value("${serpapi.api-key}")
    private String serpApiKey;

    private static final String SERPAPI_BASE_URL = "https://serpapi.com/search";

    private final RestClient restClient;

    public SerpApiServiceBackUp(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(SERPAPI_BASE_URL)
                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    public SerpApiSearchResponse search(String query) {
        String url = UriComponentsBuilder.fromUriString(SERPAPI_BASE_URL)
                .queryParam("engine", "google_news")
                .queryParam("q", query)
                .queryParam("api_key", serpApiKey)
                .queryParam("hl", "ko")
                .queryParam("gl", "kr")
                .queryParam("num", 10)  // ✅ 10개 기사만
                .queryParam("sort_by", "date") // ✅ 최신순
                .toUriString();

        System.out.println("🔍 요청 URL: " + url);

        try {
            SerpApiSearchResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(SerpApiSearchResponse.class);

            if (response == null ||  response.getNewsResults().isEmpty()) {
                System.err.println("SerpAPI가 응답을 반환하지 않았거나, 유기적 검색 결과(organic_results)가 없습니다. 쿼리: " + query);
                return new SerpApiSearchResponse();
            }

            return response;

        } catch (Exception e) {
            System.err.println("SerpAPI 호출 중 오류 발생 (쿼리: '" + query + "'): " + e.getMessage());
            e.printStackTrace();
            return new SerpApiSearchResponse();
        }
    }
}


    

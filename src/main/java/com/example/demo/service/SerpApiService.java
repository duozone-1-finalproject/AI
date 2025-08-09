package com.example.demo.service;

import com.example.demo.dto.SerpNewsItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor
public class SerpApiService {
    private final WebClient serpApiWebClient;

    @Value("${serpapi.api-key}")
    private String apiKey;
    @Value("${serpapi.default.hl:ko}") private String hl;
    @Value("${serpapi.default.gl:kr}") private String gl;

    // Google News API 호출 예시 (산업/뉴스 위험 탐지)
    public Mono<List<SerpNewsItem>> searchNews(String query) {
        return serpApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("engine", "google_news") // 뉴스 엔진 :contentReference[oaicite:14]{index=14}
                        .queryParam("q", enc(query))
                        .queryParam("hl", hl)
                        .queryParam("gl", gl)
                        .queryParam("api_key", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapNewsItems);
    }

    // Google Web 검색 (기타/회사 일반 웹 이슈)
    public Mono<List<SerpNewsItem>> searchWeb(String query) {
        return serpApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("engine", "google") // Search API 기본 엔진 :contentReference[oaicite:15]{index=15}
                        .queryParam("q", enc(query))
                        .queryParam("hl", hl)
                        .queryParam("gl", gl)
                        .queryParam("api_key", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapOrganic);
    }

    private String enc(String v) { return URLEncoder.encode(v, StandardCharsets.UTF_8); }

    @SuppressWarnings("unchecked")
    private List<SerpNewsItem> mapNewsItems(Map<String, Object> json) {
        var news = (List<Map<String, Object>>) json.getOrDefault("news_results", List.of());
        return news.stream()
                .map(m -> new SerpNewsItem(
                        (String)m.getOrDefault("title",""),
                        (String)m.getOrDefault("link",""),
                        (String)m.getOrDefault("snippet",""),
                        (String)m.getOrDefault("date","")))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<SerpNewsItem> mapOrganic(Map<String, Object> json) {
        var organic = (List<Map<String, Object>>) json.getOrDefault("organic_results", List.of());
        return organic.stream()
                .map(m -> new SerpNewsItem(
                        (String)m.getOrDefault("title",""),
                        (String)m.getOrDefault("link",""),
                        (String)m.getOrDefault("snippet",""),
                        (String)m.getOrDefault("date","")))
                .toList();
    }
}

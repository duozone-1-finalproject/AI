package com.example.demo.controller;

import com.example.demo.service.LlmService;
import com.example.demo.service.SerpApiService;
import com.example.demo.util.JsonWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SerpApiController {
    
    private final LlmService llmService;
    private final SerpApiService serpApiService;
    private static final Logger log = LoggerFactory.getLogger(SerpApiController.class);

    @GetMapping
    public ResponseEntity<?> searchWeb(@RequestParam("prompt") String prompt) {
        log.info("수신된 프롬프트: '{}'", prompt);
        try {
            // 1. LLM으로 검색 엔진 판단
            String engine = llmService.determineSearchEngine(prompt);
            log.info("판단된 검색 엔진: '{}'", engine);

            // 2. 결정된 엔진으로 검색 수행
            JsonObject searchResult = serpApiService.search(prompt, engine);

            // 3. 뉴스 검색일 경우, 특별 처리 (JSON 파일 저장)
            if ("google_news".equals(engine)) {
                JsonArray newsResults = searchResult.getAsJsonArray("news_results");
                if (newsResults != null) {
                    List<Map<String, String>> articles = StreamSupport.stream(newsResults.spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .map(obj -> {
                                // API 응답에 title이나 link가 없는 경우를 대비한 방어 코드
                                String title = obj.has("title") ? obj.get("title").getAsString() : "제목 없음";
                                String link = obj.has("link") ? obj.get("link").getAsString() : "링크 없음";
                                return Map.of("title", title, "link", link);
                            })
                            .collect(Collectors.toList());

                    JsonWriter.writeDataToFile(articles, prompt);
                    log.info("뉴스 기사 정보가 JSON 파일로 저장되었습니다. Python 크롤러를 실행하세요.");
                    return ResponseEntity.ok(Map.of("status", "success", "message", "뉴스 검색 완료. 크롤링 대기 중.", "data", articles));
                }
            }

            // 4. 뉴스 외 일반 검색 결과 반환
            return ResponseEntity.ok(Map.of("status", "success", "engine", engine, "data", searchResult.toString()));

        } catch (Exception e) {
            log.error("RAG 파이프라인 실행 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
        }
    }
}

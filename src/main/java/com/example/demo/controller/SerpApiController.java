package com.example.demo.controller;

import com.example.demo.dto.SerpApiResult;
import com.example.demo.dto.SerpApiSearchResponse;
import com.example.demo.service.LlmService;
import com.example.demo.service.SerpApiService;
import com.example.demo.util.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SerpApiController {

    private final SerpApiService serpApiService;
    private final LlmService llmService;
    private static final Logger log = LoggerFactory.getLogger(SerpApiController.class);

    @GetMapping
    public ResponseEntity<?> searchWeb(@RequestParam("prompt") String prompt) {
        log.info("수신된 프롬프트: '{}'", prompt);

        try {
            // 1. LLM으로 검색 목적 판단
            String searchType = llmService.determineSearchType(prompt);
            String engine = serpApiService.getEngineForType(searchType);
            log.info("판단된 검색 유형: '{}', 선택된 검색 엔진: '{}'", searchType, engine);

            // 2. 실제 검색 수행
            SerpApiSearchResponse response = serpApiService.search(prompt, engine);

            // 3. 결과 처리 로직 개선 (뉴스, 일반 검색 결과 모두 처리)
            List<SerpApiResult> results = null;
            if (response != null) {
                if (response.getNewsResults() != null && !response.getNewsResults().isEmpty()) {
                    results = response.getNewsResults();
                } else if (response.getOrganicResults() != null && !response.getOrganicResults().isEmpty()) {
                    // Naver, Google 일반 검색은 organic_results를 사용
                    results = response.getOrganicResults();
                }
            }

            // 4. 결과 출력 + JSON 저장
            if (results != null && !results.isEmpty()) {
                log.info("{}개의 검색 결과를 찾았습니다.", results.size());
                // 상위 10개 결과만 사용
                List<SerpApiResult> topResults = results.stream().limit(10).toList();

                topResults.forEach(result -> {
                    log.info("  - 제목: {}", result.getTitle());
                    log.info("    링크: {}", result.getLink());
                });

                try {
                    JsonWriter.writeArticlesToFile(topResults, prompt);
                } catch (IOException e) {
                    log.error("검색 결과를 파일로 저장하는 중 오류가 발생했습니다. 파일 경로와 권한을 확인해주세요.", e);
                    // 파일 저장 실패가 전체 요청을 중단시켜서는 안 됩니다. 로그만 남기고 계속 진행합니다.
                }
            } else {
                log.warn("'{}'에 대한 검색 결과를 찾을 수 없습니다.", prompt);
            }

            return ResponseEntity.ok(response);

        } catch (WebClientResponseException e) {
            log.error("SerpAPI 호출 중 오류 발생. Status: {}, Body: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "외부 API 호출에 실패했습니다. API 키 또는 네트워크를 확인해주세요."));
        } catch (Exception e) {
            log.error("알 수 없는 오류가 발생했습니다.", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부에서 처리 중 오류가 발생했습니다."));
        }
    }
}

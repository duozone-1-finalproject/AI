package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.AnalyzeRequest;
import org.opensearch.client.opensearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoriTokenServiceImpl implements NoriTokenService {
    private final OpenSearchClient client;

    // 제외할 토큰 목록
    private static final Set<String> EXCLUDED_TOKENS = Set.of(
            // 시간 단위
            "년", "월",
            // '가.', '나.' 와 같은 목록 마커
            "가", "나", "다", "라", "마", "바", "사", "아", "자", "차", "카", "타", "파", "하"
    );

    @Override
    public String join(String index, String analyzer, String text) {
        if (text == null || text.isBlank()) return "";
        var req = new AnalyzeRequest.Builder()
                .index(index)          // POST /{index}/_analyze
                .analyzer(analyzer)    // "ko_nori"
                .text(text)
                .build();
        try {
            var res = client.indices().analyze(req);
            return res.tokens().stream()
                    .map(AnalyzeToken::token)
                    .filter(token -> !token.matches("\\d+") && !EXCLUDED_TOKENS.contains(token))
                    .distinct() // 중복된 토큰을 제거합니다.
                    .collect(Collectors.joining(" "));
        } catch (IOException | RuntimeException e) { // 예기치 않은 토큰
            throw new RuntimeException("OpenSearch를 사용하여 텍스트를 분석하는 데 실패했습니다: " + e.getMessage(), e);
        }
    }
}
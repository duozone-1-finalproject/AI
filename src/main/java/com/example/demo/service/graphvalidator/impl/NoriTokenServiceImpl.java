package com.example.demo.service.graphvalidator.impl;

import com.example.demo.service.graphvalidator.NoriTokenService;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.AnalyzeRequest;
import org.opensearch.client.opensearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoriTokenServiceImpl implements NoriTokenService {
    private final OpenSearchClient client;

    // ✅ 상한선: 유니크 토큰 1000개까지만 사용 (BooleanQuery 1024 절 한도 대비 여유)
    private static final int MAX_UNIQUE_TOKENS = 1000;

    // 제외할 토큰 목록
    private static final Set<String> EXCLUDED_TOKENS = Set.of(
            // 시간 단위
            "년", "월",
            // '가.', '나.' 와 같은 목록 마커
            "가", "나", "다", "라", "마", "바", "사", "아", "자", "차", "카", "타", "파", "하"
    );

    private static final Pattern ONLY_DIGITS = Pattern.compile("\\d+");

    @Override
    public String join(String index, String analyzer, String text) {
        if (text == null || text.isBlank()) return "";

        // 1) OpenSearch Analyze API 호출 (index/analyzer 기반)  ───────────────
        var req = new AnalyzeRequest.Builder()
                .index(index)          // POST /{index}/_analyze
                .analyzer(analyzer)    // 예: "ko_nori"
                .text(text)
                .build();

        try {
            var res = client.indices().analyze(req);

            // 2) 토큰 필터링 → 유니크 1000개까지 수집  ────────────────────────
            //  - 숫자만(token) 제거
            //  - 목록마커/불용 토큰 제거
            //  - LinkedHashSet으로 "등장 순서" 유지하며 중복 제거
            LinkedHashSet<String> uniq = new LinkedHashSet<>(MAX_UNIQUE_TOKENS + 16);

            for (AnalyzeToken t : res.tokens()) {
                String tok = t.token();
                if (tok == null || tok.isBlank()) continue;
                if (ONLY_DIGITS.matcher(tok).matches()) continue;
                if (EXCLUDED_TOKENS.contains(tok)) continue;

                // (선택) 매우 짧은 1글자 토큰 배제하고 싶다면 다음 줄 주석 해제
                // if (tok.length() == 1) continue;

                uniq.add(tok);
                if (uniq.size() >= MAX_UNIQUE_TOKENS) break;  // ✅ 상한 도달 시 즉시 중단
            }

            // 3) 조인 (공백 구분)  ────────────────────────────────────────────
            //    이미 유니크/상한 처리됨
            return String.join(" ", uniq);

        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("OpenSearch Analyze 실패: " + e.getMessage(), e);
        }
    }
}
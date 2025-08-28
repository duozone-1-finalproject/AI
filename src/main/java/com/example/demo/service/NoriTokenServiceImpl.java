package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.AnalyzeRequest;
import org.opensearch.client.opensearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoriTokenServiceImpl implements NoriTokenService {
    private final OpenSearchClient client;

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
            return res.tokens().stream().map(AnalyzeToken::token).collect(Collectors.joining(" "));
        } catch (IOException | RuntimeException e) { // 예기치 않은 토큰
            throw new RuntimeException("OpenSearch를 사용하여 텍스트를 분석하는 데 실패했습니다: " + e.getMessage(), e);
        }
    }
}
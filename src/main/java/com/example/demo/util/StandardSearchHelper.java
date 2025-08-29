package com.example.demo.util;

import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StandardSearchHelper {

    private static final int MAX_DETAIL_LENGTH = 400;

    public String pickIndex(String sectionKey) {
        if (sectionKey != null && sectionKey.startsWith("risk_")) {
            return "risk_standard";
        }
        return "standard";
    }

    public List<String> pickChapIds(String sectionKey) {
        if (sectionKey == null) return List.of();
        return switch (sectionKey) {
            case "risk_industry" -> List.of("5");
            case "risk_company" -> List.of("6");
            case "risk_etc" -> List.of("7");
            default -> List.of();
        };
    }

    /**
     * OpenSearch 검색 결과를 프론트엔드에서 사용하기 쉬운 형태로 변환합니다.
     * @param hit 개별 검색 결과
     * @return 변환된 Map 객체 (id, title, detail)
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> transformHit(Hit<Map> hit) {
        Map<String, Object> src = hit.source();
        if (src == null) {
            return Collections.emptyMap();
        }

        String id = "%s-%s-%s".formatted(
                src.getOrDefault("chap_id", ""),
                src.getOrDefault("sec_id", ""),
                src.getOrDefault("art_id", "")
        );

        String title = Stream.of("chap_name", "sec_name")
                .map(key -> String.valueOf(src.getOrDefault(key, "")))
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(" - "));

        String content = String.valueOf(src.getOrDefault("content", ""));

        return Map.of("id", id, "title", title, "detail", truncate(content));
    }

    private String truncate(String s) {
        if (s == null) return "";
        if (s.length() <= StandardSearchHelper.MAX_DETAIL_LENGTH) return s;
        return s.substring(0, StandardSearchHelper.MAX_DETAIL_LENGTH) + "...";
    }
}
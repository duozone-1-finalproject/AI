package com.example.demo.dto;


public record Doc(
        String id,
        String source,     // "web" | "news" | "db"
        String title,
        String summary,    // 2~3줄 요약 (소스별 수집 노드에서 미리 압축)
        String url,
        double score       // 관련도 점수(검색 점수/재랭킹 점수)
) {
}

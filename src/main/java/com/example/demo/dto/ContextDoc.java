package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContextDoc(
        String id,
        String source,   // "web" | "news" | "db"
        String title,
        String summary,  // 2~3줄 요약(수집 노드에서 압축)
        String url,
        double score     // 관련도/재랭킹 점수
) {
    public String toBullet() {
        return "- [" + title + "] " + summary + (url != null ? " (" + url + ")" : "");
    }
}
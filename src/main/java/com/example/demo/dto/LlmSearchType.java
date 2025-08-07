package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class LlmSearchType {

    @JsonPropertyDescription("사용자 질문의 의도에 가장 적합한 검색 엔진 타입. 'google_news', 'google', 'naver' 중 하나여야 합니다.")
    private String engine;

    public String getEngine() { return engine; }

    public void setEngine(String engine) { this.engine = engine; }
}
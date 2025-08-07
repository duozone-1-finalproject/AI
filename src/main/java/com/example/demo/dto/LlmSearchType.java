package com.example.demo.dto;

/**
 * LLM의 구조화된 응답(JSON)을 파싱하기 위한 DTO입니다.
 * LLM이 다른 말을 하더라도 이 구조에 맞는 JSON만 추출하여 안정성을 보장합니다.
 */
public class LlmSearchType {
    private String category;

    // Jackson 라이브러리가 JSON을 파싱할 때 기본 생성자가 필요합니다.
    public LlmSearchType() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
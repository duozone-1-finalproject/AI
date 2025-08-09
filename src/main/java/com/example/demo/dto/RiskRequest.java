package com.example.demo.dto;

public record RiskRequest(
        String corpCode,
        String corpName,
        String industryKeyword, // 산업 키워드(검색 보조)
        String timeframe // 예: "past_12m" 등
) {}
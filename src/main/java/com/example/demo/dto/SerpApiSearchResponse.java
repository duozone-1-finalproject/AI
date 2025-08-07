package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerpApiSearchResponse {

    // 뉴스 검색 결과를 받기 위한 필드
    @JsonProperty("news_results")
    private List<SerpApiResult> newsResults;

    // 일반 검색(google, naver) 결과를 받기 위한 필드
    @JsonProperty("organic_results")
    private List<SerpApiResult> organicResults;

    // DTO 내부에 정적 중첩 클래스로 두는 것이 일반적입니다.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchParameters {
        private String q;
        private String engine;
        @JsonProperty("api_key")
        private String apiKey;
        private String num;
        private String hl;
        private String tbm;
    }
}

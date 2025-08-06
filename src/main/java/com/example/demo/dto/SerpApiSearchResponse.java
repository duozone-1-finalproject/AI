package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerpApiSearchResponse {

    // 만약 뉴스 검색 결과가 'news_results' 필드로 온다면 아래 필드를 추가하고 사용해야 합니다.
    @JsonProperty("news_results")
    private List<SerpApiResult> newsResults;

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
        private String tbm = "nws";
    }
}

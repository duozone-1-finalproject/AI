package com.example.demo.dto.graphweb;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SearchLLMDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;        // 키워드 배열 중, 1개
    private List<Item> candidates; // 해당 키워드로 수집된 문서 후보들

    @Data
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private String url;              // https://... (형식 검증은 스키마에서 regex로)
        private String topic;            // news
        private String publishedDate;    // YYYY-MM-DD
    }
}
package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SearchLLMDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 그룹 키: 허용 키워드(enum) 중 하나 */
    private String keyword;

    /** 해당 키워드로 수집된 문서 후보들 */
    private List<Item> candidates;

    @Data
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private String url;     // https://... (형식 검증은 스키마에서 regex로)
        private String source;  // 뉴스
        private String date;    // YYYY-MM-DD
    }
}
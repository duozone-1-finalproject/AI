package com.example.demo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class WebResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> queries;      // QueryBuilderNode에서 생성된 쿼리
    private List<Article> articles;    // SearchNode 결과 (링크/스니펫)
    private List<Article> beforev;     // FetchNode 이후 본문까지 포함한 결과
    //private Boolean validated; // ValidationNode 결과

    @Data
    public static class Article implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String keyword;       // 원본 키워드
        private String sectionLabel;  // "사업위험" 또는 "회사위험"
        private String title;         // 제목
        private String url;           // URL
        private String date;          // YYYY-MM-DD
        private String source;        // 뉴스|블로그|논문|정부|기타
        private String content;       // Fetch 결과 본문
    }
}

package com.example.demo.dto.graphweb;

import com.example.demo.graphweb.WebState;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class WebResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SearchNode에서 LLM의 검색 결과를 파싱하기 위해 사용됩니다.
     */
    private List<Article> candidates; // 최종 검증을 통과한 기사 목록

    public void setErrors(List<String> errors) {

    }

    @Data
    public static class Article implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String keyword;       // 원본 키워드
        private String sectionLabel;  // "사업위험" 또는 "회사위험"
        private String title;         // 제목
        private String url;           // URL
        private String date;          // YYYY-MM-DD
        private String source;        // 뉴스
        private String content;       // Fetch 결과 본문 (FetchNode에서 채워짐)
    }
}

package com.example.demo.dto; // 본인의 패키지 경로에 맞게 수정

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.net.URLDecoder; // URLDecoder 클래스 임포트
import java.nio.charset.StandardCharsets; // StandardCharsets 임포트

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerpApiResult {

    private String title;
    private String link;
    private String snippet;

    // snippet 필드의 값을 설정할 때 URL 디코딩을 수행하는 커스텀 Setter
    public void setSnippet(String snippet) {
        if (snippet != null) {
            try {
                // UTF-8 인코딩으로 URL 디코딩을 시도합니다.
                this.snippet = URLDecoder.decode(snippet, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                // 디코딩에 실패하면 원본 문자열을 그대로 유지하고 오류를 출력합니다.
                this.snippet = snippet;
                System.err.println("Warning: Failed to URL-decode snippet: " + snippet + " Error: " + e.getMessage());
            }
        } else {
            this.snippet = null;
        }
    }
}


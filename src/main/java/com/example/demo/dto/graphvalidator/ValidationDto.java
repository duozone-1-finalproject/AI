package com.example.demo.dto.graphvalidator;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ValidationDto implements Serializable {
    // Serializable 인터페이스를 구현하는 클래스는 serialVersionUID를 선언하는 것이 좋습니다.
    // 이는 클래스 버전 간의 호환성을 보장합니다.
    @Serial
    private static final long serialVersionUID = 1L;

    private String guide;    // "기업공시서식작성기준" | "투자위험요소 기재요령 안내서"
    private Quality quality;
    private String decision; // "accept" | "revise"
    private List<Issue> issues;
    private String notes;

    @Data
    public static class Quality implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer guideline_adherence;
        private Integer factuality;
        private Integer clarity;
    }

    @Data
    public static class Issue implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String span;        // 초안 내 수정대상 문장/문단 원문
        private String reason;      // 왜 문제인지(규정/논리)
        private String ruleId;      // 예: "5-1-0" (chap-sec-art)
        private String evidence;    // 지침 요지(guideHits에서)
        private String suggestion;  // 수정 가이드(명령형·간결)
        private String severity;    // low|medium|high
    }
}

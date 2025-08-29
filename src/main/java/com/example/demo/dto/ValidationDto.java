package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidationDto {
    private Quality quality;
    private String decision; // "accept" | "revise"
    private List<Issue> issues;
    private String notes;

    @Data
    public static class Quality {
        private Integer context_use;
        private Integer guideline_adherence;
        private Integer factuality;
        private Integer clarity;
    }

    @Data
    public static class Issue {
        private String span;        // 초안 내 수정대상 문장/문단 원문
        private String reason;      // 왜 문제인지(규정/논리)
        private String ruleId;      // 예: "5-1-0" (chap-sec-art)
        private String evidence;    // 지침 요지(guideHits에서)
        private String suggestion;  // 수정 가이드(명령형·간결)
        private String severity;    // low|medium|high
    }
}

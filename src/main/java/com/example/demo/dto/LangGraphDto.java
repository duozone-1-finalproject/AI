package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

public final class LangGraphDto {

    // 요청 dto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PromptRequest implements Serializable {
        @NotBlank
        private String input;
    }

    // 응답 dto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GraphResult implements Serializable {
        private String output;
    }
}

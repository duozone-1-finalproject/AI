package com.example.demo.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder // 빌더 패턴으로 객체를 쉽게 생성할 수 있게 함
public class Ai_Server_ResponseDto {
    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("generated_html")
    private String generatedHtml;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("processing_time")
    private long processingTime;

    @JsonProperty("status")
    private String status;
}
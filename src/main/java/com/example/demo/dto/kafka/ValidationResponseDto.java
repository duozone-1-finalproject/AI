package com.example.demo.dto.kafka;

import com.example.demo.dto.graphvalidator.ValidationDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponseDto {
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("status")
    private String status; // "SUCCESS", "ERROR"
    @JsonProperty("validation_result")
    private ValidationDto validationResult;
    @JsonProperty("error_message")
    private String errorMessage;
}

// src/main/java/com/example/demo/dto/kafka/VariableMappingResponseDto.java
package com.example.demo.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableMappingResponseDto {

    @JsonProperty("request_id")
    private String requestId;       // 요청 ID

    @JsonProperty("risk_industry")
    private String riskIndustry;    // S3_1A_1 변수에 들어갈 내용

    @JsonProperty("risk_company")
    private String riskCompany;     // S3_1B_1 변수에 들어갈 내용

    @JsonProperty("risk_etc")
    private String riskEtc;         // S3_1C_1 변수에 들어갈 내용

    //@JsonProperty("s1_1d_1")
    //private String s1_1d_1;         // S1_1D_1 변수에 들어갈 내용 (추가)

    @JsonProperty("processing_time")
    private Long processingTime;    // 처리 시간 (밀리초)

    @JsonProperty("status")
    private String status;          // 처리 상태 (SUCCESS, FAILED)
}
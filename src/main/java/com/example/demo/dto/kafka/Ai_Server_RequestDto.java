// src/main/java/com/example/ai_server/dto/Ai_Server_RequestDto.java
package com.example.demo.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai_Server_RequestDto {

    @JsonProperty("request_id")
    private String requestId;   // 요청 ID

    @JsonProperty("company_data")
    private Map<String, Object> companyData;

}

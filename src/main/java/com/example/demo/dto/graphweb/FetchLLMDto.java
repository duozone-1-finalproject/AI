package com.example.demo.dto.graphweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * FetchNode에서 LLM의 응답을 파싱하기 위한 DTO입니다.
 * LLM은 이 구조의 객체들을 배열 형태로 반환합니다: [ {FetchLLMDto}, {FetchLLMDto}, ... ]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchLLMDto {
    private String keyword;
    private List<WebResponseDto.Article> candidates;
}
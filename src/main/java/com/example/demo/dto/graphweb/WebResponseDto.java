package com.example.demo.dto.graphweb;

import lombok.Data;
import java.util.List;

@Data
public class WebResponseDto {
    private List<String> queries; // QueryBuilderNode에서 생성된 쿼리
    private List<String> articles; // SearchNode 결과 (링크/스니펫)
    private List<String> summaries; // SummaryNode 결과
    private Boolean validated; // ValidationNode 결과
}
// src/main/java/com/example/ai_server/kafka/KafkaReportConsumer.java
/* package com.example.demo.kafka;


import com.example.demo.dto.Ai_Server_RequestDto;
import com.example.demo.dto.Ai_Server_ResponseDto;
import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.service.GraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReportConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GraphService graphService; // 🔥 GraphService 주입

    private static final String RESPONSE_TOPIC = "ai-report-response";

    @KafkaListener(topics = "ai-report-request", groupId = "ai-server-group")
    public void consumeReportRequest(String message) {
        try {
            // 요청 JSON → DTO 변환
            Ai_Server_RequestDto request = objectMapper.readValue(message, Ai_Server_RequestDto.class);

            // Map에서 회사 정보 추출
            String corpName = extractCorpName(request.getCompanyData());
            String corpCode = extractCorpCode(request.getCompanyData());
            String indutyName = extractIndutyName(request.getCompanyData());
            String indutyCode = extractIndutyCode(request.getCompanyData());

            log.info("AI 서버 요청 수신: requestId={}, company={}, industry={}",
                    request.getRequestId(), corpName, indutyName);

            // 🔥 GraphService용 요청 DTO 생성
            DraftRequestDto draftRequest = new DraftRequestDto();
            draftRequest.setCorpCode(corpCode != null ? corpCode : "UNKNOWN");
            draftRequest.setCorpName(corpName != null ? corpName : "UNKNOWN");
            draftRequest.setIndutyCode(indutyCode != null ? indutyCode : "UNKNOWN");
            draftRequest.setIndutyName(indutyName != null ? indutyName : "UNKNOWN");
            draftRequest.setRptExist(true); // 기본값

            // 🔥 실제 AI 처리 실행
            long startTime = System.currentTimeMillis();
            DraftResponseDto aiResult = graphService.run(draftRequest);
            long processingTime = System.currentTimeMillis() - startTime;

            // 🔥 AI 결과를 HTML로 변환
            String generatedHtml = generateHtmlReport(aiResult, corpName);
            String summary = generateSummary(aiResult);

            // 응답 DTO 생성
            Ai_Server_ResponseDto response = Ai_Server_ResponseDto.builder()
                    .requestId(request.getRequestId())
                    .status("SUCCESS")
                    .summary(summary)
                    .generatedHtml(generatedHtml)
                    .processingTime(processingTime)
                    .build();

            // Kafka 응답 전송
            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(RESPONSE_TOPIC, request.getRequestId(), responseJson);

            log.info("AI 서버 응답 전송 완료: requestId={}, processingTime={}ms",
                    request.getRequestId(), processingTime);

        } catch (Exception e) {
            log.error("AI 서버 요청 처리 실패", e);

            // 에러 응답 전송
            try {
                Ai_Server_RequestDto request = objectMapper.readValue(message, Ai_Server_RequestDto.class);
                Ai_Server_ResponseDto errorResponse = Ai_Server_ResponseDto.builder()
                        .requestId(request.getRequestId())
                        .status("ERROR")
                        .summary("AI 처리 중 오류 발생: " + e.getMessage())
                        .generatedHtml("<html><body><h1>처리 실패</h1><p>오류가 발생했습니다.</p></body></html>")
                        .processingTime(0L)
                        .build();

                String errorJson = objectMapper.writeValueAsString(errorResponse);
                kafkaTemplate.send(RESPONSE_TOPIC, request.getRequestId(), errorJson);
            } catch (Exception ex) {
                log.error("에러 응답 전송 실패", ex);
            }
        }
    }

    // 🔥 회사 정보 추출 헬퍼 메서드들
    private String extractCorpName(Map<String, Object> companyData) {
        if (companyData == null) return null;
        Object overviewObj = companyData.get("companyOverview");
        if (overviewObj instanceof Map) {
            Map<String, Object> overview = (Map<String, Object>) overviewObj;
            return (String) overview.get("corpName");
        }
        return null;
    }

    private String extractCorpCode(Map<String, Object> companyData) {
        if (companyData == null) return null;
        Object overviewObj = companyData.get("companyOverview");
        if (overviewObj instanceof Map) {
            Map<String, Object> overview = (Map<String, Object>) overviewObj;
            return (String) overview.get("corpCode");
        }
        return null;
    }

    private String extractIndutyName(Map<String, Object> companyData) {
        if (companyData == null) return null;
        Object overviewObj = companyData.get("companyOverview");
        if (overviewObj instanceof Map) {
            Map<String, Object> overview = (Map<String, Object>) overviewObj;
            return (String) overview.get("indutyName");
        }
        return null;
    }

    private String extractIndutyCode(Map<String, Object> companyData) {
        if (companyData == null) return null;
        Object overviewObj = companyData.get("companyOverview");
        if (overviewObj instanceof Map) {
            Map<String, Object> overview = (Map<String, Object>) overviewObj;
            return (String) overview.get("indutyCode");
        }
        return null;
    }

    // 🔥 AI 결과를 HTML로 변환
    private String generateHtmlReport(DraftResponseDto aiResult, String corpName) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='ko'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(corpName).append(" 리스크 분석 보고서</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }");
        html.append("h2 { color: #555; margin-top: 30px; }");
        html.append(".section { margin-bottom: 20px; padding: 15px; border-left: 4px solid #007bff; background: #f8f9fa; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<h1>").append(corpName).append(" 리스크 분석 보고서</h1>");

        if (aiResult.getRiskIndustry() != null && !aiResult.getRiskIndustry().trim().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>🏭 산업 리스크</h2>");
            html.append("<p>").append(aiResult.getRiskIndustry().replace("\n", "<br>")).append("</p>");
            html.append("</div>");
        }

        if (aiResult.getRiskCompany() != null && !aiResult.getRiskCompany().trim().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>🏢 기업 리스크</h2>");
            html.append("<p>").append(aiResult.getRiskCompany().replace("\n", "<br>")).append("</p>");
            html.append("</div>");
        }

        if (aiResult.getRiskEtc() != null && !aiResult.getRiskEtc().trim().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>⚠️ 기타 리스크</h2>");
            html.append("<p>").append(aiResult.getRiskEtc().replace("\n", "<br>")).append("</p>");
            html.append("</div>");
        }

        html.append("<hr>");
        html.append("<p><small>생성일시: ").append(java.time.LocalDateTime.now()).append("</small></p>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    // 🔥 요약문 생성
    private String generateSummary(DraftResponseDto aiResult) {
        StringBuilder summary = new StringBuilder();
        summary.append("AI 리스크 분석 완료. ");

        int sections = 0;
        if (aiResult.getRiskIndustry() != null && !aiResult.getRiskIndustry().trim().isEmpty()) sections++;
        if (aiResult.getRiskCompany() != null && !aiResult.getRiskCompany().trim().isEmpty()) sections++;
        if (aiResult.getRiskEtc() != null && !aiResult.getRiskEtc().trim().isEmpty()) sections++;

        summary.append("총 ").append(sections).append("개 섹션의 리스크 분석이 생성되었습니다.");

        return summary.toString();
    }
}
*/
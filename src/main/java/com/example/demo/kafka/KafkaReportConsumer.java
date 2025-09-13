// src/main/java/com/example/demo/kafka/KafkaReportConsumer.java
package com.example.demo.kafka;

// 새로 생성한 DTO들 import
import com.example.demo.dto.kafka.VariableMappingRequestDto;
import com.example.demo.dto.kafka.VariableMappingResponseDto;
import com.example.demo.dto.graphmain.DraftRequestDto;
import com.example.demo.dto.graphmain.DraftResponseDto;
import com.example.demo.service.graphmain.GraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReportConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GraphService graphService;

    private static final String RESPONSE_TOPIC = "ai-report-response";

    @KafkaListener(topics = "ai-report-request", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeVariableMappingRequest(String message) {
        log.info("변수 매핑 요청 수신: {}", message.substring(0, Math.min(message.length(), 100)) + "...");

        try {
            // Backend 요청 JSON → DTO 변환
            VariableMappingRequestDto request = objectMapper.readValue(message, VariableMappingRequestDto.class);
            log.info("변수 매핑 처리 시작: requestId={}, corpCode={}, corpName={}, indutyCode={}, indutyName={}",
                    request.getRequestId(), request.getCorpCode(), request.getCorpName(),request.getIndutyCode(), request.getIndutyName());

            // GraphService용 요청 DTO 생성
            DraftRequestDto draftRequest = new DraftRequestDto();
            draftRequest.setCorpCode(request.getCorpCode());
            draftRequest.setCorpName(request.getCorpName());
            draftRequest.setIndutyCode(request.getIndutyCode());
            draftRequest.setIndutyName(request.getIndutyName());

            // 실제 AI 처리 실행
            long startTime = System.currentTimeMillis();
            DraftResponseDto aiResult = graphService.run(draftRequest);
            long processingTime = System.currentTimeMillis() - startTime;

            log.info("AI 처리 완료: requestId={}, processingTime={}ms",
                    request.getRequestId(), processingTime);

            // Backend가 기대하는 형식으로 응답 생성
            VariableMappingResponseDto response = VariableMappingResponseDto.builder()
                    .requestId(request.getRequestId())
                    .riskIndustry(aiResult.getRiskIndustry() != null ? aiResult.getRiskIndustry() : "산업 리스크 분석 중...")
                    .riskCompany(aiResult.getRiskCompany() != null ? aiResult.getRiskCompany() : "기업 리스크 분석 중...")
                    .riskEtc(aiResult.getRiskEtc() != null ? aiResult.getRiskEtc() : "기타 리스크 분석 중...")
                    //.s1_1d_1(generateS1_1D_1Response(aiResult)) // 추가 LLM 응답
                    .processingTime(processingTime)
                    .status("SUCCESS")
                    .build();

            // Kafka 응답 전송
            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(RESPONSE_TOPIC, request.getRequestId(), responseJson);

            log.info("변수 매핑 응답 전송 완료: requestId={}", request.getRequestId());

        } catch (Exception e) {
            log.error("변수 매핑 처리 실패", e);

            // 에러 응답 전송
            try {
                VariableMappingRequestDto request = objectMapper.readValue(message, VariableMappingRequestDto.class);
                VariableMappingResponseDto errorResponse = VariableMappingResponseDto.builder()
                        .requestId(request.getRequestId())
                        .riskIndustry("처리 실패: " + e.getMessage())
                        .riskCompany("처리 실패: " + e.getMessage())
                        .riskEtc("처리 실패: " + e.getMessage())
                        //.s1_1d_1("처리 실패: " + e.getMessage())
                        .processingTime(0L)
                        .status("FAILED")
                        .build();

                String errorJson = objectMapper.writeValueAsString(errorResponse);
                kafkaTemplate.send(RESPONSE_TOPIC, request.getRequestId(), errorJson);
            } catch (Exception ex) {
                log.error("에러 응답 전송 실패", ex);
            }
        }
    }

    /**
     * S1_1D_1 변수용 응답 생성 (추후 LLM 응답 예정)
     */
    private String generateS1_1D_1Response(DraftResponseDto aiResult) {
        // 현재는 임시 응답, 추후 별도 LLM 호출로 대체
        StringBuilder response = new StringBuilder();
        response.append("종합 분석 결과:\n");

        if (aiResult.getRiskIndustry() != null && !aiResult.getRiskIndustry().trim().isEmpty()) {
            response.append("산업 리스크 요약: ").append(extractSummary(aiResult.getRiskIndustry())).append("\n");
        }

        if (aiResult.getRiskCompany() != null && !aiResult.getRiskCompany().trim().isEmpty()) {
            response.append("기업 리스크 요약: ").append(extractSummary(aiResult.getRiskCompany())).append("\n");
        }

        response.append("종합적으로 투자 시 주의가 필요한 영역입니다.");

        return response.toString();
    }

    /**
     * 응답에서 첫 번째 문장 추출 (간단한 요약)
     */
    private String extractSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "분석 데이터 없음";
        }

        String[] sentences = text.split("\\.");
        if (sentences.length > 0) {
            return sentences[0].trim() + ".";
        }

        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }
}
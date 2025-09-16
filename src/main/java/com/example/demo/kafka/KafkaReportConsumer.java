package com.example.demo.kafka;

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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaReportConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final GraphService graphService;

    private static final String RESPONSE_TOPIC = "ai-report-response";

    // 중복 처리 방지를 위한 처리중인 요청 추적
    private final ConcurrentMap<String, Boolean> processingRequests = new ConcurrentHashMap<>();

    @KafkaListener(
            topics = "ai-report-request",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "1"  // 중복 처리 방지
    )
    public void consumeVariableMappingRequest(String message) {
        String requestId = null;

        try {
            // 요청 파싱
            VariableMappingRequestDto request = objectMapper.readValue(message, VariableMappingRequestDto.class);
            requestId = request.getRequestId();

            log.info("변수 매핑 요청 수신: requestId={}, message length={}",
                    requestId, message.length());

            // 중복 처리 체크
            if (processingRequests.putIfAbsent(requestId, true) != null) {
                log.warn("이미 처리중인 요청입니다. 무시합니다: requestId={}", requestId);
                return;
            }

            try {
                log.info("변수 매핑 처리 시작: requestId={}, corpCode={}, corpName={}, indutyCode={}, indutyName={}",
                        requestId, request.getCorpCode(), request.getCorpName(),
                        request.getIndutyCode(), request.getIndutyName());

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

                log.info("AI 처리 완료: requestId={}, processingTime={}ms", requestId, processingTime);

                // Backend가 기대하는 형식으로 응답 생성
                VariableMappingResponseDto response = VariableMappingResponseDto.builder()
                        .requestId(requestId)
                        .riskIndustry(aiResult.getRiskIndustry() != null ? aiResult.getRiskIndustry() : "산업 리스크 분석 중...")
                        .riskCompany(aiResult.getRiskCompany() != null ? aiResult.getRiskCompany() : "기업 리스크 분석 중...")
                        .riskEtc(aiResult.getRiskEtc() != null ? aiResult.getRiskEtc() : "기타 리스크 분석 중...")
                        .processingTime(processingTime)
                        .status("SUCCESS")
                        .build();

                // Kafka 응답 전송
                String responseJson = objectMapper.writeValueAsString(response);
                kafkaTemplate.send(RESPONSE_TOPIC, requestId, responseJson);

                log.info("변수 매핑 응답 전송 완료: requestId={}", requestId);

            } finally {
                // 처리 완료 후 추적 맵에서 제거
                processingRequests.remove(requestId);
            }

        } catch (Exception e) {
            log.error("변수 매핑 처리 실패: requestId={}", requestId, e);

            // 처리중 맵에서 제거
            if (requestId != null) {
                processingRequests.remove(requestId);
            }

            // 에러 응답 전송
            try {
                VariableMappingRequestDto request = objectMapper.readValue(message, VariableMappingRequestDto.class);
                VariableMappingResponseDto errorResponse = VariableMappingResponseDto.builder()
                        .requestId(request.getRequestId())
                        .riskIndustry("처리 실패: " + e.getMessage())
                        .riskCompany("처리 실패: " + e.getMessage())
                        .riskEtc("처리 실패: " + e.getMessage())
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
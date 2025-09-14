package com.example.demo.kafka;

import com.example.demo.dto.graphvalidator.CheckRequestDto;
import com.example.demo.dto.graphvalidator.ValidationDto;
import com.example.demo.dto.kafka.RevisionRequestDto;
import com.example.demo.dto.kafka.RevisionResponseDto;
import com.example.demo.dto.kafka.ValidationRequestDto;
import com.example.demo.dto.kafka.ValidationResponseDto;
import com.example.demo.service.graphvalidator.CheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.demo.constants.KafkaConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaValidationConsumer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final CheckService checkService;

    @KafkaListener(topics = VALIDATION_REQUEST_TOPIC, groupId = "ai-server-group")
    public void consumeValidationRequest(String message) {
        try {
            ValidationRequestDto request = objectMapper.readValue(message, ValidationRequestDto.class);

            String indutyName = request.getIndutyName();
            String section = request.getSection();
            String draft = request.getDraft();

            CheckRequestDto checkRequest = new CheckRequestDto();
            checkRequest.setIndutyName(indutyName != null ? indutyName : "UNKNOWN");
            checkRequest.setSection(section != null ? section : "UNKNOWN");
            checkRequest.setDraft(draft != null ? draft : "UNKNOWN");

            ValidationDto validationResult = checkService.check(checkRequest);

            ValidationResponseDto response = ValidationResponseDto.builder()
                    .requestId(request.getRequestId())
                    .status("SUCCESS")
                    .validationResult(validationResult)
                    .errorMessage(null)
                    .build();

            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(VALIDATION_RESPONSE_TOPIC, request.getRequestId(), responseJson);

        } catch (Exception e) {
            log.error("AI 서버 요청 처리 실패", e);
        }
    }


    @KafkaListener(topics = REVISION_REQUEST_TOPIC, groupId = "ai-server-group")
    public void consumeRevisionRequest(String message) {
        try {
            RevisionRequestDto request = objectMapper.readValue(message, RevisionRequestDto.class);

            ValidationDto.Issue issue = request.getIssue();

            String revisionResult = checkService.revise(issue);

            RevisionResponseDto response = RevisionResponseDto.builder()
                    .requestId(request.getRequestId())
                    .status("SUCCESS")
                    .revisedContent(revisionResult)
                    .errorMessage(null)
                    .build();

            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(REVISION_RESPONSE_TOPIC, request.getRequestId(), responseJson);

        } catch (Exception e) {
            log.error("AI 서버 요청 처리 실패", e);
        }
    }

}


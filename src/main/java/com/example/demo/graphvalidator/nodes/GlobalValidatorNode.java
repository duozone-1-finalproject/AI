package com.example.demo.graphvalidator.nodes;

import com.example.demo.dto.graphvalidator.ValidationDto;
import com.example.demo.graphvalidator.ValidatorState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.example.demo.constants.JsonSchemaConstants.VALIDATION_JSON_SCHEMA;

// 검증그래프 검증 노드
@Component("globalValidator")
@RequiredArgsConstructor
public class GlobalValidatorNode implements AsyncNodeAction<ValidatorState> {

    private static final long MAX_TRY = 3;
    @Qualifier("default")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String clip(String s) {
        if (s == null) return "";
        return s.length() <= 1000 ? s : s.substring(0, 1000) + "…";
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(ValidatorState state) {
        try {
            // 라운드 제한
            List<String> drafts = state.getDraft();
            if (drafts.size() >= MAX_TRY) return CompletableFuture.completedFuture(Map.of("decision", "end"));
            String draft = drafts.isEmpty() ? "" : drafts.getLast();
            String guideIndex = state.getGuideIndex();
            String guideLabel = switch (guideIndex) {
                case "standard" -> "기업공시서식작성기준";
                case "risk_standard" -> "투자위험요소 기재요령 안내서";
                default -> "";
            };

            List<Map<String, String>> hits = state.getGuideHits();

            String sectionLbl = state.getSectionLabel();

            // 1) 유저 템플릿 선택
            String userKey = switch (guideIndex) {
                case "standard" -> "validator_user_default";
                case "risk_standard" -> "validator_user_risk";
                default -> "validator_user_default"; // 안전 폴백
            };

            // 2) 유저 템플릿 변수 준비
            String guideCtx = hits.stream()
                    .limit(12)
                    .map(m -> {
                        String id = nvl(m.get("id"));
                        String title = nvl(m.get("title"));
                        String detail = nvl(m.get("detail"));
                        return id + "(" + title + ")::" + clip(detail);
                    })
                    .collect(Collectors.joining("\n"));

            Map<String, Object> vars = Map.of(
                    "sectionLabel", sectionLbl,
                    "draft", draft,
                    "guideLabel", guideLabel,
                    "guideCtx", guideCtx
            );

            // 3) 템플릿 → Prompt 만들기
            Prompt sysPrompt = catalog.createSystemPrompt("validator_sys", vars);    // SystemMessage 1개
            Prompt userPrompt = catalog.createPrompt(userKey, vars); // UserMessage 1개

            // 4) 메시지 병합하여 최종 Prompt 구성
            List<Message> messages = new ArrayList<>(sysPrompt.getInstructions());
            messages.addAll(userPrompt.getInstructions());

            // (3) JSON Schema 강제 (strict) 옵션 설정
            ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                    .name("ValidationDto")
                    .schema(VALIDATION_JSON_SCHEMA)
                    .strict(true)
                    .build();

            ResponseFormat rf = ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(jsonSchema)
                    .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .responseFormat(rf)
                    .build();

            Prompt finalPrompt = new Prompt(messages, options);

            // (4) 호출 & 구조화 파싱 (코드펜스 제거 불필요)
            ValidationDto vr = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(ValidationDto.class); // Spring AI가 content를 DTO로 변환

            // (5) adjust 분기용 가공
            var adjustInput = (vr.getIssues() == null ? List.<ValidationDto.Issue>of() : vr.getIssues())
                    .stream().map(i -> Map.of(
                            "span", nvl(i.getSpan()),
                            "reason", nvl(i.getReason()),
                            "ruleId", nvl(i.getRuleId()),
                            "evidence", nvl(i.getEvidence()),
                            "suggestion", nvl(i.getSuggestion()),
                            "severity", nvl(i.getSeverity())
                    )).toList();

            String method = state.getMethod();
            String decision;
            // "check" 메소드 요청일 경우, LLM의 수정 제안 여부와 관계없이 항상 그래프를 종료합니다.
            if ("check".equals(method)) {
                decision = "end";
            } else {
                // 그 외(draftValidate)의 경우, LLM의 decision에 따라 분기합니다.
                decision = "accept".equalsIgnoreCase(vr.getDecision()) ? "end" : "adjust";
            }

            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.VALIDATION, vr,
                    ValidatorState.ADJUST_INPUT, adjustInput,
                    ValidatorState.DECISION, decision
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.ERRORS, List.of("[ValidatorNode] " + e.getMessage())
            ));
        }
    }
}

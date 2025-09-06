package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// 초안생성 노드
@Slf4j
@Component("generate")
@RequiredArgsConstructor
public class DraftGeneratorNode implements AsyncNodeAction<DraftState> {

    private final PromptCatalogService catalog;

    @Qualifier("default")
    private final ChatClient chatClient;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        try {
            final String section = state.getSection();

            // 0) 공통 변수 1회 추출
            Map<String, Object> baseVars = state.getBaseVars();


            // 1) 초안 생성
            Prompt sysDraft = catalog.createSystemPrompt("draft_default", Map.of());
            Prompt userDraft = catalog.createPrompt(section, varsForDraft(section, baseVars));

            List<Message> draftMsgs = new ArrayList<>(sysDraft.getInstructions());
            draftMsgs.addAll(userDraft.getInstructions());

            // (선택) 템플릿 렌더러에 '<', '>' 구분자 및 Validation 완화 설정
            // PromptTemplate.builder()
            //   .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>')
            //      .validationMode(ValidationMode.LENIENT).build())

            logPrompt("finalPrompt (draft)", draftMsgs);
            String draftText = chatClient.prompt(new Prompt(draftMsgs)).call().content();

            // 2) 섹션별 체크리스트(자체 내부검증) 실행
            String checklistTemplate = selectChecklistTemplate(section);
            Prompt sysCheck = catalog.createSystemPrompt("check_default", Map.of()); // "JSON 금지, 한국어 문단만" 등
            Prompt userCheck = catalog.createPrompt(checklistTemplate, varsForChecklist(section, baseVars, draftText));

            List<Message> checkMsgs = new ArrayList<>(sysCheck.getInstructions());
            checkMsgs.addAll(userCheck.getInstructions());

            logPrompt("finalPrompt (checklist)", checkMsgs);
            String fixed = chatClient.prompt(new Prompt(checkMsgs)).call().content();

            // 3) 폴백
            String finalDraft = (fixed == null || fixed.isBlank()) ? draftText : fixed;
            return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, finalDraft));

        } catch (Exception e) {
            log.error("Draft generation failed", e);
            return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, ""));
        }
    }

    /** 초안용 변수: 공통에서 얕은 복사 후 섹션별로만 필요한 키 남김/추가 */
    private Map<String, Object> varsForDraft(String section, Map<String, Object> base) {
        Map<String, Object> v = new HashMap<>(base);
        // 섹션별로 실제 템플릿이 요구하는 키만 유지/보정
        switch (section) {
            case "risk_industry" -> { /* WEB_RAG, DART_RAG 사용 */ }
            case "risk_company"  -> { /* FIN_DATA 우선, WEB/DART 보조는 템플릿이 허용하면 유지 */ }
            case "risk_etc"      -> { /* OFFER_DATA(otherRiskInputs) 우선 */ }
            default -> throw new IllegalArgumentException("Unknown section: " + section);
        }
        return v;
    }

    /** 체크리스트용 변수: 공통 + draftText + 섹션별 PRIMARY/EXTERNAL 매핑 */
    private Map<String, Object> varsForChecklist(String section, Map<String, Object> base, String draftText) {
        Map<String, Object> v = new HashMap<>(base);
        v.put("draft", draftText);
        switch (section) {
            case "risk_industry" -> {
                // PRIMARY=webRagItems, EXTERNAL=dartRagItems
                // (이미 base에 있으므로 그대로 사용)
            }
            case "risk_company" -> {
                // PRIMARY=financialData, EXTERNAL=ragSources(=webRagItems)
                v.put("ragSources", base.getOrDefault("webRagItems", Collections.emptyList()));
            }
            case "risk_etc"     -> {
                // PRIMARY=otherRiskInputs, EXTERNAL=ragSources(=webRagItems)
                v.put("ragSources", base.getOrDefault("webRagItems", Collections.emptyList()));
            }
        }
        return v;
    }

    private String selectChecklistTemplate(String section) {
        return switch (section) {
            case "risk_industry" -> "risk_industry_checklist";
            case "risk_company"  -> "risk_company_checklist";
            case "risk_etc"      -> "risk_etc_checklist";
            default -> throw new IllegalArgumentException("Unknown section: " + section);
        };
    }

    private void logPrompt(String title, List<Message> messages) {
        String body = messages.stream()
                .map(m -> "[" + m.getMessageType() + "] " + String.valueOf(m.getText()))
                .collect(Collectors.joining("\n---\n"));
        log.info("\n===== {} =====\n{}\n=======================\n", title, body);
    }
}
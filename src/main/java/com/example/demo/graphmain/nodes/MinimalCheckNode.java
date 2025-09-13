package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

// 초안생성 -> 내부 미니멀 체크리스트 검증(컨텍스트 사용여부, 출처표기, 줄내림 등 검토)
@Slf4j
@Component("minimal_check")
@RequiredArgsConstructor
public class MinimalCheckNode implements AsyncNodeAction<DraftState> {

    private final PromptCatalogService catalog;
    private static final ObjectMapper OM = new ObjectMapper();

    @Qualifier("default")
    private final ChatClient chatClient;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        try {
            final String section = state.getSection();

            // 0) 공통 변수 1회 추출
            Map<String, Object> baseVars = state.getBaseVars();

            String draftText = state.getDrafts().getLast();
            log.debug("[MinimalCheckNode] BEFORE - draft: {}", draftText);


            // 2) 섹션별 체크리스트(자체 내부검증) 실행
            String checklistTemplate = selectChecklistTemplate(section);
            Prompt sysCheck = catalog.createSystemPrompt("check_sys", Map.of());
            Prompt userCheck = catalog.createPrompt(checklistTemplate, varsForChecklist(section, baseVars, draftText));

            List<Message> checkMsgs = new ArrayList<>(sysCheck.getInstructions());
            checkMsgs.addAll(userCheck.getInstructions());

            Prompt checkPrompt = new Prompt(checkMsgs);
            String fixed = chatClient.prompt(checkPrompt).call().content();
            log.debug("[MinimalCheckNode] AFTER - fixed: {}", fixed);



            // 3) 폴백
            String finalDraft = (fixed == null || fixed.isBlank()) ? draftText : fixed;
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.DRAFT, List.of(finalDraft)
            ));

        } catch (Exception e) {
            log.error("Draft generation failed", e);
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.DRAFT, List.of(),
                    DraftState.ERRORS, List.of("[MinimalCheckNode] " + e.getMessage())
            ));
        }
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
}
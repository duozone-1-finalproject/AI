package com.example.demo.langgraph.nodes;

import com.example.demo.dto.ValidationDto;
import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// 기업공시작성기준 검증 노드
@Component("globalValidator")
@RequiredArgsConstructor
public class GlobalValidatorNode implements AsyncNodeAction<DraftState> {

    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;


    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        try {
            // 라운드 제한
            List<String> drafts = state.<List<String>>value(DraftState.DRAFT).orElse(List.of());
            if (drafts.size() >= 4) return CompletableFuture.completedFuture(Map.of("decision","end"));

            String draft = drafts.isEmpty() ? "" : drafts.getLast();
            String guideIndex = state.<String>value(DraftState.GUIDE_INDEX).orElse("standard");
            @SuppressWarnings("unchecked")
            List<Map<String,String>> hits = state.<List<Map<String,String>>>value(DraftState.GUIDE_HITS).orElse(List.of());

            String section     = state.<String>value(DraftState.SECTION).orElse("");
            String sectionLbl  = state.<String>value(DraftState.SECTION_LABEL).orElse("");

            // 1) 시스템 템플릿 선택
            String sysKey = "standard".equals(guideIndex)
                    ? "validator_default"
                    : "validator_risk";

            // 2) 유저 템플릿 변수 준비
            String guideCtx = hits.stream()
                    .limit(12)
                    .map(m -> "- " + m.getOrDefault("title","") + " :: " + m.getOrDefault("detail",""))
                    .collect(Collectors.joining("\n"));

            Map<String,Object> vars = Map.of(
                    "section", section,
                    "sectionLabel", sectionLbl,
                    "draft", draft,
                    "guideCtx", guideCtx
            );

            // 3) 템플릿 → Prompt 만들기
            Prompt sysPrompt  = catalog.createSystemPrompt(sysKey, vars);    // SystemMessage 1개
            Prompt userPrompt = catalog.createPrompt("validator_user", vars); // UserMessage 1개

            // 4) 메시지 병합하여 최종 Prompt 구성
            List<Message> messages = new ArrayList<>(sysPrompt.getInstructions());
            messages.addAll(userPrompt.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            // 5) 호출 & 파싱
            String json = chatClient.prompt(finalPrompt).call().content();
            json = json.replaceAll("```json\\s*","").replaceAll("```","").trim();

            ValidationDto vr = om.readValue(json, ValidationDto.class);
            var adjustInput = (vr.getIssues()==null? List.<ValidationDto.Issue>of(): vr.getIssues())
                    .stream().map(i -> Map.of(
                            "span",        nvl(i.getSpan()),
                            "reason",      nvl(i.getReason()),
                            "ruleId",      nvl(i.getRuleId()),
                            "evidence",    nvl(i.getEvidence()),
                            "suggestion",  nvl(i.getSuggestion()),
                            "severity",    nvl(i.getSeverity())
                    )).toList();

            String decision = "accept".equalsIgnoreCase(vr.getDecision()) ? "end" : "adjust";
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.VALIDATION, vr,
                    DraftState.ADJUST_INPUT, adjustInput,
                    DraftState.DECISION, decision
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.ERRORS, List.of("[ValidatorNode] " + e.getMessage())
            ));
        }
    }
    private static String nvl(String s){ return s==null? "": s; }
}

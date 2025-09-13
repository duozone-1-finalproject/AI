package com.example.demo.validatorgraph.nodes;

import com.example.demo.validatorgraph.ValidatorState;
import com.example.demo.service.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component("adjust")
@RequiredArgsConstructor
public class AdjustDraftNode implements AsyncNodeAction<ValidatorState> {
    @Qualifier("default")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;

    @Override
    public CompletableFuture<Map<String, Object>> apply(ValidatorState state) {
        try {
            // 입력 가져오기
            List<String> drafts = state.<List<String>>value(ValidatorState.DRAFT).orElse(List.of());
            String draft = drafts.isEmpty() ? "" : drafts.getLast();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = state.<List<Map<String, Object>>>value(ValidatorState.ADJUST_INPUT).orElse(List.of());

            String section = state.<String>value(ValidatorState.SECTION).orElse("");
            String sectionLbl = state.<String>value(ValidatorState.SECTION_LABEL).orElse("");

            // 이슈 직렬화
            String issuesText = serializeIssues(issues);

            // 템플릿 변수
            Map<String, Object> vars = Map.of(
                    "section", section,
                    "sectionLabel", sectionLbl,
                    "draft", draft,
                    "issuesText", issuesText
            );

            // 프롬프트(시스템+유저) 조합
            Prompt sys = catalog.createSystemPrompt("adjust_default", vars);
            Prompt user = catalog.createPrompt("adjust_user", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            // 호출
            String revised = chatClient.prompt(finalPrompt).call().content();

            return CompletableFuture.completedFuture(Map.of(ValidatorState.DRAFT, revised));

        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.ERRORS, List.of("[AdjustDraftNode] " + e.getMessage()),
                    ValidatorState.DECISION, "end"
            ));
        }
    }

    private String serializeIssues(List<Map<String, Object>> issues) {
        if (issues == null || issues.isEmpty()) return "- (수정 지시 없음)";
        final String SEP = "-----";
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Map<String, Object> it : issues) {
            String span = String.valueOf(it.getOrDefault("span", "")).trim();
            String reason = String.valueOf(it.getOrDefault("reason", "")).trim();
            String ruleId = String.valueOf(it.getOrDefault("ruleId", "")).trim();
            String evidence = String.valueOf(it.getOrDefault("evidence", "")).trim();
            String suggestion = String.valueOf(it.getOrDefault("suggestion", "")).trim();
            String severity = String.valueOf(it.getOrDefault("severity", "")).trim();

            sb.append(SEP).append(" ISSUE #").append(i++).append("\n");
            sb.append("span: ").append(span).append("\n");
            sb.append("reason: ").append(reason).append("\n");
            if (!ruleId.isEmpty()) sb.append("ruleId: ").append(ruleId).append("\n");
            if (!evidence.isEmpty()) sb.append("evidence: ").append(evidence).append("\n");
            sb.append("suggestion: ").append(suggestion).append("\n");
            if (!severity.isEmpty()) sb.append("severity: ").append(severity).append("\n");
        }
        sb.append(SEP).append(" END");
        return sb.toString();
    }
}

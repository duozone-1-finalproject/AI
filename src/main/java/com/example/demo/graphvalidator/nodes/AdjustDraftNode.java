package com.example.demo.graphvalidator.nodes;

import com.example.demo.graphvalidator.ValidatorState;
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

@Slf4j
@Component("adjust")
@RequiredArgsConstructor
public class AdjustDraftNode implements AsyncNodeAction<ValidatorState> {

    @Qualifier("default")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(ValidatorState state) {
        try {
            // 입력 가져오기
            List<String> drafts = state.getDraft();
            String draft = drafts.isEmpty() ? "" : drafts.getLast();
            List<Map<String, Object>> issues = state.getAdjustInput();
            log.debug("[AdjustDraftNode] issuse: {}", issues);

            String sectionLbl = state.getSectionLabel();

            List<Map<String, String>> minimal = issues.stream()
                    .map(it -> Map.of(
                            "span", String.valueOf(it.getOrDefault("span","")),
                            "suggestion", String.valueOf(it.getOrDefault("suggestion","")),
                            "severity", String.valueOf(it.getOrDefault("severity",""))
                    ))
                    .toList();

            String issuesJson = om.writeValueAsString(minimal);

            // 템플릿 변수 교체
            Map<String, Object> vars = Map.of(
                    "sectionLabel", sectionLbl,
                    "draft", draft,
                    "issues", issuesJson    // <-- adjust_user.st에서 issues := <issues>
            );

            Prompt sys  = catalog.createSystemPrompt("adjust_sys", vars);
            Prompt user = catalog.createPrompt("adjust_user", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            // 호출
            String revised = chatClient.prompt(finalPrompt).call().content();

            return CompletableFuture.completedFuture(Map.of(ValidatorState.DRAFT, List.of(revised)));

        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.ERRORS, List.of("[AdjustDraftNode] " + e.getMessage()),
                    ValidatorState.DECISION, "end"
            ));
        }
    }
}

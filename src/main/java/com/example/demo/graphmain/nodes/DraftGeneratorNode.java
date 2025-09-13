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
            Prompt sysDraft = catalog.createSystemPrompt("draft_sys", Map.of());
            Prompt userDraft = catalog.createPrompt(section, baseVars);

            List<Message> draftMsgs = new ArrayList<>(sysDraft.getInstructions());
            draftMsgs.addAll(userDraft.getInstructions());
            Prompt draftPrompt = new Prompt(draftMsgs);
            String draftText = chatClient.prompt(draftPrompt).call().content();

            return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, List.of(draftText)));

        } catch (Exception e) {
            log.error("Draft generation failed", e);
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.DRAFT, List.of(),
                    DraftState.ERRORS, List.of("[DraftGeneratorNode] " + e.getMessage())
            ));
        }
    }
}
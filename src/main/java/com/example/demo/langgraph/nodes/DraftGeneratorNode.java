package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String section = state.<String>value(DraftState.SECTION).orElseThrow();

        // 템플릿 변수
        Map<String, Object> vars = new HashMap<>();
        vars.put("corpName",  state.<String>value(DraftState.CORP_NAME).orElse(""));
        vars.put("indutyName",state.<String>value(DraftState.IND_NAME ).orElse(""));
        vars.put("webRagItems", "Map.of()");
        vars.put("dartRagItems", Map.of());
        vars.put("maxItems", 5);


        // 프롬프트(시스템+유저) 조합
        Prompt sys = catalog.createSystemPrompt("draft_default", Map.of());
        Prompt user = catalog.createPrompt(section, vars);

        List<Message> messages = new ArrayList<>(sys.getInstructions());
        messages.addAll(user.getInstructions());
        Prompt finalPrompt = new Prompt(messages);

        // pretty print: [SYSTEM]/[USER] 블록으로 구분해서 전체 프롬프트 로깅
        String promptLog = messages.stream()
                .map(m -> "[" + m.getMessageType() + "] " + String.valueOf(m.getText()))
                .collect(Collectors.joining("\n---\n"));
        log.info("\n===== finalPrompt =====\n{}\n=======================", promptLog);

        // 호출
        String text = chatClient.prompt(finalPrompt).call().content();

        return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, text));
    }
}

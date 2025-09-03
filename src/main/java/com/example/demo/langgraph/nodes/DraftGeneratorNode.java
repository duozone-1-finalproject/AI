package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 초안생성 노드
@Component("generate")
@RequiredArgsConstructor
public class DraftGeneratorNode implements AsyncNodeAction<DraftState> {

    private final PromptCatalogService catalog;

    @Qualifier("default")
    private final ChatClient chatClient;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String section = state.<String>value(DraftState.SECTION).orElseThrow();

        Map<String, Object> vars = new HashMap<>();
        vars.put("corpName",  state.<String>value(DraftState.CORP_NAME).orElse(""));
        vars.put("indutyName",state.<String>value(DraftState.IND_NAME ).orElse(""));

        // 시스템 프롬프트로 호출(필요 시 createPrompt로 변경)
        Prompt prompt = catalog.createSystemPrompt(section, vars);

        // 동기 호출(스트리밍/리액티브도 가능)
        String text = chatClient.prompt(prompt).call().content();  // 결과 텍스트 추출

        return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, text));
    }
}

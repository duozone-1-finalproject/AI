package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 초안생성 노드
@Component("generate")
@RequiredArgsConstructor
public class DraftGeneratorNode implements AsyncNodeAction<DraftState> {

    private final PromptCatalogService catalog;
    private final ChatClient chatClient;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String section = state.<String>value(DraftState.SECTION).orElseThrow();

        // 템플릿 변수
        Map<String, Object> vars = new HashMap<>();
        vars.put("corpName",  state.<String>value(DraftState.CORP_NAME).orElse(""));
        vars.put("indutyName",state.<String>value(DraftState.IND_NAME ).orElse(""));
        vars.put("webRagItems", Map.of());
        vars.put("dartRagItems", Map.of());
        vars.put("maxItems", 5);


        // 프롬프트(시스템+유저) 조합
        Prompt sys = catalog.createSystemPrompt("draft_default", Map.of());
        Prompt user = catalog.createPrompt(section, vars);

        List<Message> messages = new ArrayList<>(sys.getInstructions());
        messages.addAll(user.getInstructions());
        Prompt finalPrompt = new Prompt(messages);

        // 호출
        String text = chatClient.prompt(finalPrompt).call().content();

        return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, text));
    }
}

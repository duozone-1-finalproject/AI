package com.example.demo.graphweb.nodes;

import com.example.demo.graphweb.WebState;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessNode implements AsyncNodeAction<WebState> {
    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        String fetched = state.getFetchedArticles();

        // 2) 템플릿 변수 바인딩
        Map<String, Object> vars = new HashMap<>();
        vars.put("fetched", fetched);

        // 3) 프롬프트 만들기(시스템/유저). 템플릿 없으면 간단 문자열로 대체
        Prompt sys = catalog.createSystemPrompt("preprocess_sys", vars);
        Prompt user = catalog.createPrompt("preprocess_user", vars);

        List<Message> messages = new ArrayList<>(sys.getInstructions());
        messages.addAll(user.getInstructions());

        Prompt finalPrompt = new Prompt(messages);

//          4) ChatClient 호출 → 본문 String
        String fetchedText = chatClient.prompt(finalPrompt).call().content();
        log.info("[ProcessNode] 전처리 텍스트: {}", fetchedText);

        return CompletableFuture.completedFuture(Map.of(WebState.PRO_ARTICLES, fetchedText));
    }
}

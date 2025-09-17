package com.example.demo.graphweb.nodes;

import com.example.demo.dto.graphweb.SearchLLMDto;
import com.example.demo.graphweb.WebState;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;

    // searchResults 0번 인덱스의 candidates의 0번인덱스 만 꺼내서 (SearchLLMDto.Item)
    // map. vars  프롬프트 변수 넣고 -> string 반환하고(chatclient)(본문) state 하나 만들어서 거기에 리턴 문자열

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        SearchLLMDto.Item pickedAticle = state.getPickedArticle();

        // 2) 템플릿 변수 바인딩
        Map<String, Object> vars = new HashMap<>();
        vars.put("title", pickedAticle.getTitle());
        vars.put("url", pickedAticle.getUrl());
        // =https://mk.co.kr/news/it/11289426
//        vars.put("title", "32년만에 ‘1등’ 바뀌었다...반도체 왕국 삼성마저 제친 이 기업은 어디");
//        vars.put("url", "https://mk.co.kr/news/it/11289426");

        // 3) 프롬프트 만들기(시스템/유저). 템플릿 없으면 간단 문자열로 대체
        Prompt sys = catalog.createSystemPrompt("fetch_sys", vars);
        Prompt user = catalog.createPrompt("fetch_user", vars);

        List<Message> messages = new ArrayList<>(sys.getInstructions());
        messages.addAll(user.getInstructions());

        Prompt finalPrompt = new Prompt(messages);

//          4) ChatClient 호출 → 본문 String
        String fetchedText = chatClient.prompt(finalPrompt).call().content();
        log.info("[FetchNode] fetchedText: {}", fetchedText);

        return CompletableFuture.completedFuture(Map.of(WebState.FETCHED_ARTICLES, fetchedText));
    }
}

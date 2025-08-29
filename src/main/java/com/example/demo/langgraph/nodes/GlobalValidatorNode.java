package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 기업공시작성기준 검증 노드
@Component("globalValidator")
@RequiredArgsConstructor
public class GlobalValidatorNode implements AsyncNodeAction<DraftState> {

    private final ChatClient chatClient;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        return null;
    }
}

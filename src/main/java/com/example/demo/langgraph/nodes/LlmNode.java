package com.example.demo.langgraph.nodes;

import com.example.demo.dto.LangGraphDto;
import com.example.demo.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 기존에 사용했던 LLM 노드 삭제예정 (예시 참고용으로 임시로 냅두는 중)
@Component("llm")
@RequiredArgsConstructor
public class LlmNode implements AsyncNodeAction<AgentState> {

    private final LlmService llmService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(AgentState agentState) {
        LangGraphDto.PromptRequest req = agentState.value("input")
                .map(LangGraphDto.PromptRequest.class::cast)
                .orElse(new LangGraphDto.PromptRequest(""));

        String answer = llmService.complete(
                // DTO가 Lombok 클래스라면 getInput(), record면 input()
                req.getInput()
        );

        return CompletableFuture.completedFuture(
                Map.of("output", new LangGraphDto.GraphResult(answer))
        );
    }
}

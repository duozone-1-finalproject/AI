package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component("prompt")
@RequiredArgsConstructor
public class PromptSelectorNode implements AsyncNodeAction<DraftState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        return null;
    }

}

package com.example.demo.langgraph.nodes;

// RAG 소스 선택 노드
import com.example.demo.langgraph.DraftState;
import com.example.demo.service.SourcePolicyService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.langgraph.DraftState.SECTION;
import static com.example.demo.langgraph.DraftState.SOURCES;

@Component("source_select")
@RequiredArgsConstructor
public class SourceSelectorNode implements AsyncNodeAction<DraftState> {

    private final SourcePolicyService policy;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String sectionKey = state.<String>value(SECTION)
                .orElseThrow(() -> new IllegalStateException("SECTION missing"));

        List<String> sources = policy.sourcesFor(sectionKey); // 예: ["news","db"]

        return CompletableFuture.completedFuture(Map.of(SOURCES, sources));
    }
}
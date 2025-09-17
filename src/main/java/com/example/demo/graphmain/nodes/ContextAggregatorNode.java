package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// RAG 소스들 병합하기전에 수행하는 노드
@Slf4j
@Component("aggregate")
@RequiredArgsConstructor
public class ContextAggregatorNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        List<String> sources = state.getSources();

        boolean dbReady  = !sources.contains("db")
                || (state.getDbDocs()  != null && !state.getDbDocs().isEmpty());
        boolean webReady = !sources.contains("web")
                || (state.getWebDocs() != null && !state.getWebDocs().isEmpty());

        Map<String, Object> updates = Map.of(
                DraftState.DB_READY, dbReady,
                DraftState.WEB_READY, webReady
        );
        log.debug("[ContextAggregatorNode] {} DB_READY: {}", state.getSectionLabel(),  dbReady);
        log.debug("[ContextAggregatorNode] {} WEB_READY: {}", state.getSectionLabel(), webReady);


        return CompletableFuture.completedFuture(updates);
    }
}

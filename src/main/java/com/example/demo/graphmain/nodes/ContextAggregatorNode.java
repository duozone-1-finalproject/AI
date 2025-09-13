package com.example.demo.graphmain.nodes;

import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// RAG 소스들 병합하기전에 수행하는 노드
@Slf4j
@Component("aggregate")
@RequiredArgsConstructor
public class ContextAggregatorNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        boolean dbReady = !state.getDbDocs().isEmpty();
//        boolean webReady  = !state.getWebDocs().isEmpty();
//        boolean newsReady = !state.getNewsDocs().isEmpty();

        Map<String, Object> updates = Map.of(
                DraftState.DB_READY, dbReady
//                DraftState.WEB_READY, webReady,
//                DraftState.NEWS_READY, newsReady
        );
        log.debug("[ContextAggregatorNode] DB_READY: {}", dbReady);
//        log.info("[ContextAggregatorNode] WEB_READY: {}", webReady);


        return CompletableFuture.completedFuture(updates);
    }
}

package com.example.demo.langgraph.nodes;

import com.example.demo.dto.dbsubgraph.DbDocDto;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// RAG 소스들 병합하기전에 수행하는 노드
@Component("aggregate")
@RequiredArgsConstructor
public class ContextAggregatorNode implements AsyncNodeAction<DraftState> {

    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        boolean dbReady = !state.dbDocs().isEmpty();
//        boolean webReady  = !state.webDocs().isEmpty();
//        boolean newsReady = !state.newsDocs().isEmpty();

        Map<String, Object> updates = Map.of(
                DraftState.DB_READY, dbReady
//                DraftState.WEB_READY, webReady,
//                DraftState.NEWS_READY, newsReady
        );

        return CompletableFuture.completedFuture(updates);
    }
}

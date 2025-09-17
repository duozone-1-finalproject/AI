package com.example.demo.graphmain.nodes;

import com.example.demo.graphdb.DbSubGraphState;
import com.example.demo.dto.graphdb.DbDocDto;
import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component("db_branch")
@RequiredArgsConstructor
public class DbSubgraphInvoker implements AsyncNodeAction<DraftState> {

    @Autowired
    private CompiledGraph<DbSubGraphState> dbSubGraph;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        List<String> sources = state.getSources();
        if (sources == null || !sources.contains("db")) {
            // 선택되지 않았으면 아무 것도 안 함 (no-op)
            return CompletableFuture.completedFuture(Map.of(DraftState.DB_READY, true));
        }
        // DraftState -> DbSubGraphState로 매핑
        Map<String, Object> subStateInit = new HashMap<>();
        subStateInit.put(DbSubGraphState.SECTION, state.getSection());
        subStateInit.put(DbSubGraphState.CORP_CODE, state.getCorpCode());
        subStateInit.put(DbSubGraphState.IND_CODE, state.getIndutyCode());

        // SubGraph 실행
        DbSubGraphState finalState = dbSubGraph.invoke(subStateInit).orElse(new DbSubGraphState(Map.of()));

        // SubGraph 결과에서 DB_DOCS, financials 추출
        List<DbDocDto> dbDocs = finalState.getDbDocs();
        log.debug("[DbSubgraphInvoker] dbDocs: {}", dbDocs);
        String financials = finalState.getFinancials();
        log.debug("[DbSubgraphInvoker] financials: {}", financials);


        return CompletableFuture.completedFuture(Map.of(
                DraftState.DB_DOCS, dbDocs,
                DraftState.FINANCIALS, financials
        ));
    }
}

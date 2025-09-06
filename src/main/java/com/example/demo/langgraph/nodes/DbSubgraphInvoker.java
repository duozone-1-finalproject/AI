package com.example.demo.langgraph.nodes;

import com.example.demo.dbsubgraph.DbSubGraphState;
import com.example.demo.dto.dbsubgraph.DbDocDto;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component("db_branch")
@RequiredArgsConstructor
public class DbSubgraphInvoker implements AsyncNodeAction<DraftState> {

    @Autowired
    private CompiledGraph<DbSubGraphState> dbSubGraph;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        // DraftState -> DbSubGraphState로 매핑
        Map<String, Object> subStateInit = new HashMap<>();
        subStateInit.put(DbSubGraphState.SECTION, state.<String>value(DraftState.SECTION).orElse(""));
        subStateInit.put(DbSubGraphState.CORP_CODE, state.<String>value(DraftState.CORP_CODE).orElse(""));
        subStateInit.put(DbSubGraphState.IND_CODE, state.<String>value(DraftState.IND_CODE).orElse(""));

        // SubGraph 실행
        return CompletableFuture.supplyAsync(() -> {
            DbSubGraphState subGraphResult = dbSubGraph.invoke(subStateInit)
                    .orElse(new DbSubGraphState(Map.of()));

            // SubGraph 결과에서 DB_DOCS 추출
            List<DbDocDto> dbDocs = subGraphResult.getDbDocs();
            String financials = subGraphResult.getFinancials();

            return Map.of(
                    DraftState.DB_DOCS, dbDocs,
                    DraftState.FINANCIALS, financials
            );
        });
    }
}

package com.example.demo.dbsubgraph.nodes;

import com.example.demo.dbsubgraph.DbSubGraphState;
import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.dbsubgraph.DbDocDto;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestDbSubgraphinvoker {
    @Autowired
    private CompiledGraph<DbSubGraphState> dbSubGraph;

    public List<DbDocDto> runOne(String sectionKey, DraftRequestDto req) {
        // 그래프 초기 상태 구성
        Map<String, Object> subStateInit = new HashMap<>();
        subStateInit.put(DbSubGraphState.SECTION, sectionKey);
        subStateInit.put(DbSubGraphState.CORP_CODE, req.getCorpCode());
        subStateInit.put(DbSubGraphState.IND_CODE, req.getIndutyCode());

        // 그래프 실행 → 최종 상태에서 초안 텍스트 추출
        DbSubGraphState finalState = dbSubGraph.invoke(subStateInit).orElse(new DbSubGraphState(Map.of()));  // compile()된 그래프는 invoke/stream 가능
        return (List<DbDocDto>) finalState.value(DbSubGraphState.DB_DOCS).orElse("");
    }

    public String runTest(String sectionKey, DraftRequestDto req) {
        // 그래프 초기 상태 구성
        Map<String, Object> subStateInit = new HashMap<>();
        subStateInit.put(DbSubGraphState.SECTION, sectionKey);
        subStateInit.put(DbSubGraphState.CORP_CODE, req.getCorpCode());
        subStateInit.put(DbSubGraphState.IND_CODE, req.getIndutyCode());

        // 그래프 실행 → 최종 상태에서 초안 텍스트 추출
        DbSubGraphState finalState = dbSubGraph.invoke(subStateInit).orElse(new DbSubGraphState(Map.of()));  // compile()된 그래프는 invoke/stream 가능
        return finalState.value(DbSubGraphState.FINANCIALS).orElse("").toString();
    }
}

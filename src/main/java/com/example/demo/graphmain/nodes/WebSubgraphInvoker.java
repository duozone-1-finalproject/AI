package com.example.demo.graphmain.nodes;

import com.example.demo.graphweb.WebState;
import com.example.demo.dto.graphweb.WebDocs;
import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component("web_branch")
@RequiredArgsConstructor
public class WebSubgraphInvoker implements AsyncNodeAction<DraftState> {

    private final CompiledGraph<WebState> webSubGraph;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        List<String> sources = state.getSources();
        if (sources == null || !sources.contains("web")) {
            // 선택되지 않았으면 아무 것도 안 함 (no-op)
            return CompletableFuture.completedFuture(Map.of(DraftState.WEB_READY, true));
        }
        // 1. DraftState -> WebState로 매핑 (웹 서브그래프에 필요한 초기값 전달)
        // WebState의 정의에 따라 corpName, indutyName, sectionLabel을 전달합니다.
        Map<String, Object> subStateInit = new HashMap<>();
        subStateInit.put(WebState.CORP_NAME, state.getCorpName());
        subStateInit.put(WebState.IND_NAME, state.getIndutyName());
        subStateInit.put(WebState.SECTION_LABEL, state.getSectionLabel());

        // 2. Web SubGraph 실행
        WebState finalState = webSubGraph.invoke(subStateInit).orElse(new WebState(Map.of()));

        // 3. SubGraph 결과에서 최종 문서 목록(webDocs) 추출
        List<WebDocs> webDocs = finalState.getWebDocs();
        log.debug("[WebSubgraphInvoker] webDocs count: {}", webDocs.size());

        // 4. 메인 그래프(DraftState)에 결과 전달
        return CompletableFuture.completedFuture(Map.of(
                DraftState.WEB_DOCS, webDocs
        ));
    }
}

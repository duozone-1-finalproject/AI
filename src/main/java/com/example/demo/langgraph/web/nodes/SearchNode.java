//SearchNode에서 DuckService를 주입받아 사용하도록 코드 만들어야함
// querynode에서 정의된 키워드 가지고 ....
//QueryBuilderNode에서 만든 쿼리를 받아서 DuckDuckGo API 호출
// → 기사 스니펫/링크를 가져와 state에 저장.
// 실제 검색

package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.service.DuckService;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SearchNode implements AsyncNodeAction<DraftState> {

    private final DuckService duckService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String section = state.<String>value(DraftState.SECTION).orElse("");
        List<String> queries = state.<List<String>>value(DraftState.PROMPT).orElse(List.of());

        // 결과 컨테이너
        List<String> webDocs = new ArrayList<>();
        List<String> newsDocs = new ArrayList<>();

        for (String query : queries) {
            if (DraftState.SECTION_RISK_INDUSTRY.equals(section)) {
                // 산업위험 → 뉴스만
                newsDocs.addAll(duckService.searchNews(query, 3, "date"));
            } else if (DraftState.SECTION_RISK_COMPANY.equals(section)) {
                // 사업위험 → 웹
                webDocs.addAll(duckService.searchWeb(query, 3, "date"));
            }
            // SECTION_RISK_ETC 등 다른 섹션에 대한 로직 추가 가능
        }

        // state 업데이트
        Map<String, Object> partial = new HashMap<>();
        partial.put(DraftState.WEB_DOCS, webDocs);
        partial.put(DraftState.NEWS_DOCS, newsDocs);

        return CompletableFuture.completedFuture(partial);
    }
}

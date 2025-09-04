//SearchNode에서 DuckService를 주입받아 사용하도록 코드 만들어야함
// querynode에서 정의된 키워드 가지고 ....
//QueryBuilderNode에서 만든 쿼리를 받아서 DuckDuckGo API 호출
// → 기사 스니펫/링크를 가져와 state.set(WebState.ARTICLES)에 저장.
// 실제 검색

package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.service.DuckService;
import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SearchNode implements Node<WebState> {

    private final DuckService duckService;

    @Override
    public CompletableFuture<WebState> apply(WebState state) {
        String section = state.value(WebState.SECTION).orElse("").toString();
        List<String> queries = (List<String>) state.value(WebState.QUERY).orElse(List.of());
        List<String> articles = new ArrayList<>();

        for (String query : queries) {
            if (WebState.SECTION_RISK_INDUSTRY.equals(section)) {
                // 산업위험 → 뉴스 검색만
                articles.addAll(duckService.searchNews(query, 3, "date"));
            } else if (WebState.SECTION_RISK_COMPANY.equals(section)) {
                // 회사위험 → 뉴스 + 웹 검색
                articles.addAll(duckService.searchNews(query, 3, "date"));
                articles.addAll(duckService.searchWeb(query, 3, "date"));
            }
        }

        // state에 검색 결과 저장
        Map<String, Object> partial = new HashMap<>();
        partial.put(WebState.ARTICLES, articles);
        state.update(partial, WebState.SCHEMA);

        return CompletableFuture.completedFuture(state);
    }
}



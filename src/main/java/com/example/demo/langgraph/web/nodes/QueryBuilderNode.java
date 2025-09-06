//섹션(산업위험/회사위험)에 따라 키워드 4개씩 조합해서 쿼리 리스트를 만듦.
// List of랑 new ArrayList랑 다른점은?
// 동적으로 섹션별 다른 키워드를 넣고 싶으면 → new ArrayList<>()
// 항상 같은 고정 키워드 세트라면 → List.of(...)
// 이 파일은 그냥 사용할 키워드 정의라고 생각하기

package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class QueryBuilderNode implements AsyncNodeAction<WebState> {

    @Value("#{${risk.business-keyword-map}}")
    private Map<String, String> businessKeywordMap;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        String company = state.value(WebState.CORP_NAME).orElse("").toString();
        String industry = state.value(WebState.IND_NAME).orElse("").toString();
        String section = state.value(WebState.SECTION_LABEL).orElse("").toString();

        List<String> queries = new ArrayList<>();

        if ("사업 위험".equals(section)) {
            // 매핑에서 키워드 후보 가져오기
            for (Map.Entry<String, String> entry : businessKeywordMap.entrySet()) {
                List<String> candidates = Arrays.asList(entry.getValue().split(","));
                Collections.shuffle(candidates);
                // 최대 2~3개만 랜덤 샘플링
                List<String> selected = candidates.subList(0, Math.min(2, candidates.size()));
                for (String keyword : selected) {
                    queries.add(company + " " + industry + " " + keyword.trim());
                }
            }
        } else if ("산업 위험".equals(section)) {
            // 산업 위험은 고정 키워드 예시
            queries.add(company + " 재무 악화");
            queries.add(company + " 수익성 악화");
        }

        state.set(WebState.QUERY, queries);
        System.out.println("[QueryBuilderNode] 생성된 쿼리: " + queries);

        return CompletableFuture.completedFuture((Map<String, Object>) state);
    }
}



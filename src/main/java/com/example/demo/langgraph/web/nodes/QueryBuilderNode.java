//섹션(산업위험/회사위험)에 따라 키워드 4개씩 조합해서 쿼리 리스트를 만듦.
// List of랑 new ArrayList랑 다른점은?
// 동적으로 섹션별 다른 키워드를 넣고 싶으면 → new ArrayList<>()
// 항상 같은 고정 키워드 세트라면 → List.of(...)
// 이 파일은 그냥 사용할 키워드 정의라고 생각하기

package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class QueryBuilderNode implements Node<WebState> {

    @Override
    public CompletableFuture<WebState> apply(WebState state) {
        String company = state.value(WebState.CORP_NAME).orElse("").toString();
        String industry = state.value(WebState.IND_NAME).orElse("").toString();
        String section = state.value(WebState.SECTION).orElse("").toString();

        List<String> queries = new ArrayList<>();

        // 섹션별 조건 분기
        if ("사업 위험".equals(section)) {
            queries.add(company + " " + industry + " 시장 전망");
            queries.add(company + " " + industry + " 규제 변화");
            queries.add(company + " " + industry + " 기술 혁신");
            queries.add(company + " " + industry + " 연구개발 투자");
        } else if ("산업 위험".equals(section)) {
            queries.add(company + " 재무 악화");
            queries.add(company + " 수익성 악화");
            queries.add(company + " 자산 부실");
            queries.add(company + " 투자 부담");
        } else {
            System.out.println("[QueryBuilderNode] 알 수 없는 섹션: " + section);
        }

        // state에 쿼리 저장
        state.set(WebState.QUERY, queries);

        System.out.println("[QueryBuilderNode] 생성된 쿼리: " + queries);

        return CompletableFuture.completedFuture(state);
    }
}


//섹션(산업위험/회사위험)에 따라 키워드 4개씩 조합해서 쿼리 리스트를 만듦.
// List of랑 new ArrayList랑 다른점은?
// 동적으로 섹션별 다른 키워드를 넣고 싶으면 → new ArrayList<>()
// 항상 같은 고정 키워드 세트라면 → List.of(...)
// 이 파일은 그냥 사용할 키워드 정의라고 생각하기

package com.example.demo.webgraph.nodes;

import com.example.demo.constants.KeywordContants;
import com.example.demo.langgraph.state.DraftState;
import com.example.demo.webgraph.state.WebState;
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
        String section = state.value(WebState.SECTION_LABEL).orElse("").toString();

        List<String> queries = new ArrayList<>(
                switch (section) {
                    case "사업위험"      -> KeywordContants.BUS_KWD; // ex) List<String>
                    case "회사위험"      -> KeywordContants.COM_KWD; // ex) List<String>
                    case "기타 투자위험" -> List.<String>of();                // 빈 리스트
                    default             -> List.<String>of();                // 방어 로직
                }
        );

        System.out.println("[QueryBuilderNode] 생성된 쿼리: " + queries);

        return CompletableFuture.completedFuture(Map.of(WebState.QUERY, queries));
    }
}



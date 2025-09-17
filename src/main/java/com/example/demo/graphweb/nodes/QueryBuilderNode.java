//섹션(산업위험/회사위험)에 따라 키워드 4개씩 조합해서 쿼리 리스트를 만듦.
// List of랑 new ArrayList랑 다른점은?
// 동적으로 섹션별 다른 키워드를 넣고 싶으면 → new ArrayList<>()
// 항상 같은 고정 키워드 세트라면 → List.of(...)
// 이 파일은 그냥 사용할 키워드 정의라고 생각하기
/**
 * QueryBuilderNode 클래스는 LangGraph의 노드(Node)로 동작하며,
 * 회사명, 산업명, 섹션 구분값(section_label)에 따라 검색 쿼리 리스트를 생성한다.
 *
 * - section_label 값이 "사업위험"이면 BUSINESS_KEYWORDS를,
 * "회사위험"이면 COMPANY_KEYWORDS를 사용한다.
 * - 각 키워드를 회사명과 산업명과 결합하여 최종 검색 쿼리를 만든다.
 * 예: "삼성전자 반도체 시장 전망"
 * - 만들어진 쿼리 리스트는 WebState.QUERY 채널에 partial update 형태로 저장된다.
 */
package com.example.demo.graphweb.nodes;

import com.example.demo.graphweb.WebState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryBuilderNode implements AsyncNodeAction<WebState> {

    private static final List<String> BUSINESS_KEYWORDS = List.of("시장 전망", "산업 동향");
    private static final List<String> COMPANY_KEYWORDS  = List.of("재무 상태", "지배 구조");

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        // 입력된 회사명, 산업명, 섹션 라벨 읽기
        String corp  = state.getCorpName();
        String ind   = state.getIndName();
        String label = state.getSectionLabel(); // "사업위험" | "회사위험"

        List<String> base = switch (label) {
            case "사업위험" -> BUSINESS_KEYWORDS;
            case "회사위험" -> COMPANY_KEYWORDS;
            default -> List.of();
        };

        List<String> queries = base.stream()
                // 최종 쿼리 문자열 생성 (회사명 + 산업명 + 키워드)
                .map(k -> corp + " " + ind + " " + k) // 최종 쿼리 문자열
                .toList();
        log.info("[QueryBuilderNode] queries: {}", queries);

        // WebState.QUERY 채널에 쿼리 리스트를 partial update로 저장
        return CompletableFuture.completedFuture(
                Map.of(WebState.QUERY, queries)
        );
    }
}



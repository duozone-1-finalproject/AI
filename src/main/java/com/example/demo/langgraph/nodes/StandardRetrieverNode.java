package com.example.demo.langgraph.nodes;

// 지침(기업공시작성기준/투자위험요소 기재요령 안내서) 검색노드 -> state에 배열로 저장
// e.g. [{"id":"2-1-0", "source":"risk_standard", "title":"투자위험요소의 정의(chap_name): 정의(sec_name)", "summary":"핵심투자위험요소란..."}]
// 기본 Default지침 = 기업공시작성기준(standard 인덱스) / 투자위험요소 섹션들 = 투자위험요소 기재요령 안내서

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.langgraph.nodes.utils.StandardSearchHelper;
import com.example.demo.service.NoriTokenService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TermsQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component("standardRetriever")
@RequiredArgsConstructor
public class StandardRetrieverNode implements AsyncNodeAction<DraftState> {

    private final OpenSearchClient client;
    private final NoriTokenService noriTokenService;
    private final StandardSearchHelper standardSearchHelper;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        try {
            String sectionLabel = state.<String>value(DraftState.SECTION_LABEL).orElse("");
            String index = standardSearchHelper.pickIndex(sectionLabel);       // standard | risk_standard
            List<String> chapIds = standardSearchHelper.pickChapIds(sectionLabel);     // [5]/[6]/[7] or []

            String draft = state.<String>value(DraftState.DRAFT).orElse("");
            String joined = noriTokenService.join(index, "ko_nori", draft);
            String queryString = sectionLabel.isBlank() ? joined : sectionLabel + " " + joined;

            var req = buildSearchRequest(index, queryString, chapIds);

            SearchResponse<Map> resp = client.search(req, Map.class);

            var guideHits = resp.hits().hits().stream().map(standardSearchHelper::transformHit).toList();

            return CompletableFuture.completedFuture(Map.of(
                    DraftState.GUIDE_INDEX, index,
                    DraftState.GUIDE_HITS, guideHits
            ));
        } catch (Exception e) {
            // 에러 발생 시, 상태에 에러 메시지를 기록하고 계속 진행
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.ERRORS, "[StandardRetrieverNode] " + e.getMessage()
            ));
        }
    }

    private SearchRequest buildSearchRequest(String index, String queryString, List<String> chapIds) {
        Query multiMatchQuery = Query.of(q -> q.multiMatch(new MultiMatchQuery.Builder()
                .query(queryString)
                .fields("sec_name^2", "chap_name^1.5", "content")
                .operator(Operator.Or)
                .build()));

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder()
                .should(multiMatchQuery)
                .minimumShouldMatch("1");

        if (!chapIds.isEmpty()) {
            TermsQuery termsQuery = new TermsQuery.Builder().field("chap_id")
                    .terms(t -> t.value(chapIds.stream().map(FieldValue::of).toList())).build();
            boolQueryBuilder.filter(Query.of(q -> q.terms(termsQuery)));
        }

        return new SearchRequest.Builder()
                .index(index).size(12)
                .source(s -> s.filter(f -> f.includes("chap_id", "chap_name", "sec_id", "sec_name", "art_id", "content", "art_name")))
                .query(Query.of(q -> q.bool(boolQueryBuilder.build())))
                .build();
    }
}
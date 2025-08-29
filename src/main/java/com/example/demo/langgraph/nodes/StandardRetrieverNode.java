package com.example.demo.langgraph.nodes;

// 지침(기업공시작성기준/투자위험요소 기재요령 안내서) 검색노드 -> state에 배열로 저장
// e.g. [{"id":"2-1-0", "source":"risk_standard", "title":"투자위험요소의 정의(chap_name): 정의(sec_name)", "summary":"핵심투자위험요소란..."}]
// 기본 Default지침 = 기업공시작성기준(standard 인덱스) / 투자위험요소 섹션들 = 투자위험요소 기재요령 안내서

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.util.StandardSearchHelper;
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
            String sectionKey = state.<String>value(DraftState.SECTION).orElse("");
            String sectionLabel = state.<String>value(DraftState.SECTION_LABEL).orElse("");
            String index = standardSearchHelper.pickIndex(sectionKey);       // standard | risk_standard
            List<String> chapIds = standardSearchHelper.pickChapIds(sectionKey);     // [5]/[6]/[7] or []

            // DRAFT는 appender 채널이므로 List<String> 타입입니다. 가장 마지막 초안을 가져옵니다.
            List<String> drafts = state.<List<String>>value(DraftState.DRAFT).orElse(List.of());
            String draft = drafts.isEmpty() ? "" : drafts.getLast();
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
        final Query query;

        // queryString이 비어있으면 MultiMatchQuery에서 예외가 발생하므로, match_none 쿼리를 사용합니다.
        if (queryString == null || queryString.isBlank()) {
            query = Query.of(q -> q.matchNone(mn -> mn));
        } else {
            Query multiMatchQuery = Query.of(q -> q.multiMatch(new MultiMatchQuery.Builder()
                    .query(queryString)
                    .fields("sec_name^2", "content")
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
            query = Query.of(q -> q.bool(boolQueryBuilder.build()));
        }

        return new SearchRequest.Builder()
                .query(query)
                .index(index).size(12)
                .source(s -> s.filter(f -> f.includes("chap_id", "chap_name", "sec_id", "sec_name", "art_id", "content")))
                .build();
    }
}
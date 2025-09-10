package com.example.demo.graphvalidator.nodes;

import com.example.demo.graphvalidator.ValidatorState;
import com.example.demo.service.graphvalidator.NoriTokenService;
import com.example.demo.util.StandardSearchHelper;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component("standardRetriever")
@RequiredArgsConstructor
public class StandardRetrieverNode implements AsyncNodeAction<ValidatorState> {

    private final OpenSearchClient client;
    private final NoriTokenService noriTokenService;
    private final StandardSearchHelper standardSearchHelper;

    @Override
    public CompletableFuture<Map<String, Object>> apply(ValidatorState state) {
        try {
            String sectionKey = state.getSection();
            String sectionLabel = state.getSectionLabel();
            String index = standardSearchHelper.pickIndex(sectionKey);       // standard | risk_standard
            List<String> chapIds = standardSearchHelper.pickChapIds(sectionKey);     // [5]/[6]/[7] or []

            // DRAFT는 appender 채널이므로 List<String> 타입입니다. 가장 마지막 초안을 가져옵니다.
            List<String> drafts = state.getDraft();
            String draft = drafts.isEmpty() ? "" : drafts.getLast();
            String joined = noriTokenService.join(index, "ko_nori", draft);
            String queryString = sectionLabel.isBlank() ? joined : sectionLabel + " " + joined;

            SearchRequest req = buildSearchRequest(index, queryString, chapIds);
            SearchResponse<Map> resp = client.search(req, Map.class);

            List<Map<String, String>> guideHits = resp.hits().hits().stream()
                    .map(standardSearchHelper::transformHit) // {id,title,detail}
                    .toList();

            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.GUIDE_INDEX, index,
                    ValidatorState.GUIDE_HITS, guideHits
            ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    ValidatorState.ERRORS, List.of("[StandardRetrieverNode] " + e.getMessage())
            ));
        }
    }

    private SearchRequest buildSearchRequest(String index, String queryString, List<String> chapIds) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        // risk_standard인 경우에만 chap 필터 적용
        if (chapIds != null && !chapIds.isEmpty()) {
            TermsQuery terms = new TermsQuery.Builder()
                    .field("chap_id") // keyword 권장
                    .terms(t -> t.value(chapIds.stream().distinct().map(FieldValue::of).toList()))
                    .build();
            bool.filter(Query.of(q -> q.terms(terms))); // filter: 비스코어 + 캐시 우호적
        }

        // 항상 multi_match 사용 (label 보장)
        bool.must(Query.of(q -> q.multiMatch(new MultiMatchQuery.Builder()
                .query(queryString)
                .fields("sec_name^2", "content")
                .operator(Operator.Or)
                .minimumShouldMatch("1")        // 필요 시 규칙식으로 조정 가능
                .build()
        )));

        return new SearchRequest.Builder()
                .index(index)
                .query(Query.of(q -> q.bool(bool.build())))
                .size(12)
                .source(s -> s.filter(f -> f.includes("chap_id","chap_name","sec_id","sec_name","art_id","content")))
                .build();
    }
}
package com.example.demo.opensearch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.InnerHits; // ★ inner_hits 빌더는 여기
import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ReportSearchService {

    private final OpenSearchClient client;

    public ReportSearchService(OpenSearchClient client) {
        this.client = client;
    }

    /** 문서 색인(Map 기반) – 개발환경에선 refresh(waitFor/True)로 즉시 조회 */
    public IndexResponse indexDoc(String index, String id, Map<String, Object> source) throws IOException {
        IndexRequest<Map<String, Object>> req = IndexRequest.<Map<String, Object>>of(b -> b
                .index(index)
                .id(id)
                .document(source)
                .refresh(Refresh.True)   // 필요 시 Remove 가능
        );
        return client.index(req);
    }

    /** Basic Search (match_all) */
    public List<ObjectNode> searchAll(String index, int size) throws IOException {
        SearchResponse<ObjectNode> res = client.search(s -> s
                        .index(index)
                        .size(size)
                        .query(q -> q.matchAll(m -> m)),
                ObjectNode.class); // ★ 가이드 권장: raw JSON일 때 ObjectNode 사용
        return res.hits().hits().stream().map(Hit::source).toList();
    }

    /** match 쿼리 (예: 본문 검색) */
    public List<ObjectNode> searchByMatch(String index, String field, String text, int size) throws IOException {
        SearchRequest req = new SearchRequest.Builder()
                .index(index)
                .size(size)
                .query(q -> q.match(m -> m.field(field).query(FieldValue.of(text))))
                .build();
        SearchResponse<ObjectNode> res = client.search(req, ObjectNode.class);
        return res.hits().hits().stream().map(Hit::source).toList();
    }

    /**
     * nested + inner_hits 예시
     * - sections.sec_title/sections.sec_content 에서 키워드 검색
     * - corp_code 정확 필터(term)
     * - pub_date 내림차순 정렬
     */
    public SearchResponse<ObjectNode> searchInSections(
            String index, String corpCode, String keyword, int size) throws IOException {

        // term 필터 (정확값)
        Query corpFilter = new Query.Builder()
                .term(t -> t.field("corp_code").value(FieldValue.of(corpCode)))
                .build();

        // nested 질의 + inner_hits
        Query nested = new Query.Builder()
                .nested(n -> n
                        .path("sections")
                        .query(nq -> nq.bool(nb -> nb
                                .should(sh -> sh.match(mm -> mm.field("sections.sec_title").query(FieldValue.of(keyword))))
                                .should(sh -> sh.match(mm -> mm.field("sections.sec_content").query(FieldValue.of(keyword))))
                                .minimumShouldMatch("1")
                        ))
                        .innerHits(ih -> ih
                                .name("sections_hits")
                                .size(5)                       // 필요 시 sort/_source 필터 추가
                        )
                )
                .build();

        SearchRequest req = new SearchRequest.Builder()
                .index(index)
                .size(size)
                .trackTotalHits(t -> t.enabled(true))
                .sort(s -> s.field(f -> f.field("pub_date").order(SortOrder.Desc)))
                .query(q -> q.bool(b -> b.must(corpFilter).must(nested)))
                .build();

        return client.search(req, ObjectNode.class);
    }

    /** 집계 예시: title.keyword terms agg */
    public SearchResponse<ObjectNode> aggregateTitles(String index, String matchField, String text) throws IOException {
        SearchRequest req = new SearchRequest.Builder()
                .index(index)
                .query(q -> q.match(m -> m.field(matchField).query(FieldValue.of(text))))
                .aggregations("titles", a -> a.terms(t -> t.field("title.keyword")))
                .build();
        return client.search(req, ObjectNode.class);
    }

    // --- (참고) 하이브리드 쿼리 ---
    // OpenSearch 3.x + opensearch-java 3.x 이상에서만 HybridQuery/NeuralQuery를 쓸 수 있어.
    // 2.19.0에선 하이브리드가 미지원/제약 이슈가 있었음. 필요하면 3.x로 업그레이드해 아래 패턴으로 사용 권장.
    /*
    public SearchResponse<ObjectNode> searchHybrid3x(String index, String text, String kw, String modelId) throws IOException {
        Query hybrid = Query.of(h -> h.hybrid(q -> q.queries(List.of(
                MatchQuery.of(m -> m.field("text").query(FieldValue.of(text)))._toQuery(),
                TermQuery.of(t -> t.field("passage_text").value(FieldValue.of(kw)))._toQuery(),
                NeuralQuery.of(n -> n.field("passage_embedding").queryText(text).modelId(modelId).k(100))._toQuery()
        ))));
        SearchRequest req = new SearchRequest.Builder().index(index).query(hybrid).build();
        return client.search(req, ObjectNode.class);
    }
    */
}
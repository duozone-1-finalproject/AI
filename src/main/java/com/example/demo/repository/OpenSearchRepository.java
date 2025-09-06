package com.example.demo.repository;

import com.example.demo.constants.DbSubGraphConstants;
import com.example.demo.dto.graphdb.RawDocDto;
import com.example.demo.dto.graphdb.TotalDocDto;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.demo.util.graphdb.FinancialUtils.toDouble;

@Repository
@RequiredArgsConstructor
public class OpenSearchRepository {

    private final OpenSearchClient client;

    // query를 통해 해당 회사의 corp_code 얻어오기 -> ex) 재무정보 비슷한 회사 가져오기
    public List<String> fetchPeerCorpCodes(Query query, String indexName) throws IOException {

        SearchResponse<Map> response = client.search(s -> s
                        .index(indexName)
                        .size(1)
                        .source(s1 -> s1.filter(f -> f.includes("corp_code")))
                        .query(query),
                Map.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(source -> (String) source.get("corp_code"))
                .filter(Objects::nonNull)
                .toList();
    }

    // corpCodes의 위험요소 내용 가져오기 -> sectionTitle로 세부 검색 (ex- 사업위험, 회사위험, 기타위험)
    public List<String> fetchSectionContents(List<String> corpCodes, String sectionTitle) throws IOException {

        SearchResponse<TotalDocDto> response = client.search(s -> s
                        .index("rpt_sec_eq")
                        .source(s1 -> s1.fetch(false))
                        .size(1)
                        .sort(sort -> sort.field(f -> f.field("pub_date").order(SortOrder.Desc)))
                        .query(q -> q.bool(b -> b
                                .must(m -> m.terms(t -> t.field("corp_code").terms(t1 -> t1.value(corpCodes.stream()
                                        .map(FieldValue::of).collect(Collectors.toList())))))
                                .must(m -> m.nested(n -> n
                                        .path("sections")
                                        .query(nq -> nq.bool(nb -> nb
                                                .should(sh -> sh.matchPhrase(mp -> mp
                                                        .field("sections.sec_title")
                                                        .query(sectionTitle)
                                                        .boost(1.0f)
                                                ))
                                        ))
                                        .innerHits(ih -> ih
                                                .source(s2 -> s2.filter(f -> f.includes("sections.sec_content")))
                                        )
                                ))
                        )),
                TotalDocDto.class
        );

        return response.hits().hits().stream()
                .flatMap(hit -> hit.innerHits().get("sections").hits().hits().stream())
                .map(innerHit -> ((JsonData) innerHit.source()).to(RawDocDto.class).getSec_content())
                .filter(Objects::nonNull)
                .toList();
    }


    // 재무정보 가져오기
    public List<Map<String, Object>> fetchStandardAccounts(String corpCode) throws IOException {
        String indexName = "fin_" + corpCode;
        SearchResponse<Map> response = client.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .terms(t -> t
                                        .field("account_nm")
                                        .terms(tt -> tt.value(
                                                DbSubGraphConstants.STANDARD_ACCOUNTS.stream()
                                                        .map(FieldValue::of)
                                                        .toList()
                                        ))
                                )
                        )
                        .source(src -> src.filter(f -> f.includes(
                                "corp_code", "account_nm", "thstrm_amount", "frmtrm_amount"
                        ))),
                Map.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(source -> (Map<String, Object>) source)
                .toList();
    }



    public Map<String, Map<String, Double>> fetchAllAccount(String corpCode) throws IOException {
        String indexName = "fin_" + corpCode;

        SearchResponse<Map> response = client.search(s -> s
                        .index(indexName)
                        .size(10000) // 충분히 크게 설정
                        .query(q -> q.matchAll(m -> m)) // match_all 쿼리
                        .source(src -> src.filter(f -> f.includes(
                                "corp_code", "account_nm", "thstrm_amount", "frmtrm_amount", "bfefrmtrm_amount"
                        ))),
                Map.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        src -> (String) src.get("account_nm"),
                        src -> Map.of(
                                "thstrm", toDouble(src.get("thstrm_amount")),
                                "frmtrm", toDouble(src.get("frmtrm_amount")),
                                "bfefrmtrm", toDouble(src.get("bfefrmtrm_amount"))
                        ),
                        (oldVal, newVal) -> newVal
                ));
    }


}

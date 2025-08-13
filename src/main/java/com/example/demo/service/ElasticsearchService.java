package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 7.17.x Java API Client 기반 검색 서비스
 * - standard 인덱스: match + highlight (작성기준 스니펫)
 * - 공시 보고서 인덱스(rpt_*): nested(sections) + inner_hits + highlight
 * - 회사 필터(term), 최신(pub_date desc) 정렬, _source 최소화
 */
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchClient es;

    /**
     * 기업공시작성기준 인덱스(standard)에서 키워드로 본문(content) 검색
     * - content 하이라이트 스니펫을 _snippet 키로 포함
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchStandardByKeyword(String keyword, int size) throws IOException {

        // 하이라이트 설정: content 필드에서 1개 프래그먼트만
        Highlight highlight = Highlight.of(h -> h
                .fields("content", HighlightField.of(hf -> hf
                        .preTags("<em>").postTags("</em>")
                        .fragmentSize(160)
                        .numberOfFragments(1)))
        );

        SearchRequest req = SearchRequest.of(s -> s
                .index("standard")
                .size(size)
                .query(q -> q.match(m -> m
                        .field("content")
                        .query(keyword)
                ))
                .highlight(highlight)
                // _source 최소화 (필요시 필드 추가)
                .source(src -> src.filter(f -> f
                        .includes("chap_id", "chap_name", "sec_id", "sec_name", "art_id", "art_name", "content")))
        );

        SearchResponse<Map> resp = es.search(req, Map.class);

        return resp.hits().hits().stream().map(h -> {
            Map<String, Object> src = (Map<String, Object>) h.source();
            if (src == null) src = new LinkedHashMap<>();

            // 하이라이트 스니펫 주입
            if (h.highlight() != null && h.highlight().get("content") != null
                    && !h.highlight().get("content").isEmpty()) {
                src.put("_snippet", h.highlight().get("content").get(0));
            }
            return src;
        }).collect(Collectors.toList());
    }

    /**
     * 공시보고서 섹션(nested) 검색 - 기존 시그니처 유지 (기간필터 없음)
     * - indexes: rpt_biz, rpt_qt, rpt_half, rpt_sec_eq 등
     * - corpName 필터(있으면), keyword 매칭
     */
    public List<Map<String, Object>> searchReportsSection(
            String[] indexes, String corpName, String keyword, int size) throws IOException {
        return searchReportsSection(indexes, corpName, keyword, size, null, null);
    }

    /**
     * 공시보고서 섹션(nested) 검색 - 기간 필터 추가 버전
     * - pub_date: yyyyMMdd 문자열 기준 gte/lte
     * - 결과 Map에는 top-level 메타 + _inner_hits(섹션 스니펫 리스트) 포함
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchReportsSection(
            String[] indexes, String corpName, String keyword, int size, String dateFrom, String dateTo) throws IOException {

        // nested(sections) 매치 + inner_hits(섹션 조각만 수집)
        NestedQuery.Builder nestedBuilder = new NestedQuery.Builder()
                .path("sections")
                .query(nq -> nq.match(m -> m
                        .field("sections.sec_content")
                        .query(keyword)
                ))
                .innerHits(new InnerHits.Builder()
                        .name("sec_hits")
                        .size(3)
                        .highlight(h -> h
                                .fields("sections.sec_content", HighlightField.of(hf -> hf
                                        .preTags("<em>").postTags("</em>")
                                        .fragmentSize(180)
                                        .numberOfFragments(1)
                                )))
                        // 섹션 필드만 소스 포함
                        .source(src -> src.filter(f -> f
                                .includes("sections.sec_title", "sections.sec_content")))
                        .build()
                );

        // bool 쿼리: 회사 필터(term) + 기간 필터(range) + nested must
        Query boolQuery = Query.of(q -> q.bool(b -> {
            b.must(m -> m.nested(nestedBuilder.build()));

            if (corpName != null && !corpName.isBlank()) {
                b.filter(f -> f.term(TermQuery.of(t -> t
                        .field("corp_name")
                        .value(corpName)
                )));
            }

            // 기간 필터(pub_date: yyyyMMdd) - 옵션
            if (dateFrom != null || dateTo != null) {
                b.filter(f -> f.range(RangeQuery.of(r -> {
                    r.field("pub_date");
                    if (dateFrom != null) r.gte(JsonData.of(dateFrom));
                    if (dateTo != null) r.lte(JsonData.of(dateTo));
                    return r;
                })));
            }
            return b;
        }));

        // 하이라이트는 inner_hits 쪽에서 처리. 상위 문서의 _source는 최소화
        SearchRequest req = SearchRequest.of(s -> s
                .index(indexes)
                .size(size)
                .query(boolQuery)
                .sort(so -> so.field(sf -> sf.field("pub_date").order(SortOrder.Desc)))
                .source(src -> src.filter(f -> f
                        .includes("doc_id", "doc_name", "doc_code", "pub_date", "corp_code", "corp_name")))
        );

        SearchResponse<Map> resp = es.search(req, Map.class);

        List<Map<String, Object>> results = new ArrayList<>();

        for (Hit<Map> hit : resp.hits().hits()) {
            Map<String, Object> parent = (Map<String, Object>) hit.source();
            if (parent == null) parent = new LinkedHashMap<>();

            // inner_hits에서 매칭된 섹션들 추출
            List<Map<String, Object>> secSnippets = new ArrayList<>();
            Map<String, InnerHitsResult> ihMap = hit.innerHits();

            if (ihMap != null && ihMap.get("sec_hits") != null) {
                var innerHits = ihMap.get("sec_hits").hits().hits();

                innerHits.forEach(inner -> {
                    // inner.source() -> JsonData 이므로 Map으로 변환
                    Map<String, Object> section = inner.source() != null
                            ? inner.source().to(Map.class) : Map.of();

                    // 하이라이트(fragment) 우선 사용
                    String snippet = null;
                    if (inner.highlight() != null
                            && inner.highlight().get("sections.sec_content") != null
                            && !inner.highlight().get("sections.sec_content").isEmpty()) {
                        snippet = inner.highlight().get("sections.sec_content").getFirst();
                    }
                    if (snippet == null) {
                        snippet = String.valueOf(section.getOrDefault("sec_content", ""));
                    }

                    Map<String, Object> one = new LinkedHashMap<>();
                    one.put("sec_title", section.getOrDefault("sec_title", ""));
                    one.put("snippet", snippet);
                    secSnippets.add(one);
                });
            }

            // 결과 문서에 섹션 스니펫 묶음 추가
            parent.put("_inner_hits", secSnippets);
            results.add(parent);
        }

        return results;
    }
}

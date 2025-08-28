package com.example.demo.langgraph.nodes;

// 지침(기업공시작성기준/투자위험요소 기재요령 안내서) 검색노드 -> state에 배열로 저장
// e.g. [{"id":"2-1-0", "source":"risk_standard", "title":"투자위험요소의 정의(chap_name): 정의(sec_name)", "summary":"핵심투자위험요소란..."}]
// 기본 Default지침 = 기업공시작성기준(standard 인덱스) / 투자위험요소 섹션들 = 투자위험요소 기재요령 안내서
import com.example.demo.langgraph.state.DraftState;
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
import java.util.stream.Collectors;

@Component("standardRetriever")
@RequiredArgsConstructor
public class StandardRetrieverNode implements AsyncNodeAction<DraftState> {

    private final OpenSearchClient client;

    private static final String KEY_GUIDE_INDEX = DraftState.GUIDE_INDEX;   // "guideIndex"
    private static final String KEY_GUIDE_HITS  = DraftState.GUIDE_HITS;    // "guideHits"

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        try {
            final String sectionLabel = state.<String>value(DraftState.SECTION_LABEL).orElse("");
            final String index        = pickIndex(sectionLabel);
            final List<String> chapIds= pickChapIds(sectionLabel); // 로컬에서만 사용

            // 전처리 없이: sectionLabel + 공백 + tokens 공백 join
            List<String> tokens = readTokens(state);
            final String queryString = buildQueryString(sectionLabel, tokens);

            // multiMatch + (위험 섹션이면) chap_id filter
            Query multiMatch = Query.of(q -> q.multiMatch(
                    new MultiMatchQuery.Builder()
                            .query(queryString)
                            .fields(Arrays.asList("sec_name^2", "chap_name^1.5", "content"))
                            .operator(Operator.Or)
                            .build()
            ));

            BoolQuery.Builder bool = new BoolQuery.Builder()
                    .should(multiMatch)
                    .minimumShouldMatch("1");

            if (!chapIds.isEmpty()) {
                TermsQuery tq = new TermsQuery.Builder()
                        .field("chap_id")
                        .terms(t -> t.value(chapIds.stream().map(FieldValue::of).toList()))
                        .build();
                bool.filter(Query.of(q -> q.terms(tq)));
            }

            List<String> includes = new ArrayList<>(List.of(
                    "chap_id","chap_name","sec_id","sec_name","art_id","content"
            ));
            if ("standard".equals(index)) includes.add("art_name");

            SearchRequest req = new SearchRequest.Builder()
                    .index(index)
                    .size(12)
                    .source(s -> s.filter(f -> f.includes(includes)))
                    .query(Query.of(q -> q.bool(bool.build())))
                    .build();

            SearchResponse<Map> resp = client.search(req, Map.class);

            List<Map<String, String>> guideHits = resp.hits().hits().stream().map(h -> {
                Map<String, Object> src = (Map<String, Object>) h.source();
                String chapId  = String.valueOf(src.getOrDefault("chap_id", ""));
                String secId   = String.valueOf(src.getOrDefault("sec_id", ""));
                String artId   = String.valueOf(src.getOrDefault("art_id", ""));
                String chapNm  = String.valueOf(src.getOrDefault("chap_name", ""));
                String secNm   = String.valueOf(src.getOrDefault("sec_name", ""));
                String artName = String.valueOf(src.getOrDefault("art_name", ""));

                String id = String.format("%s-%s-%s", chapId, secId, artId);
                String title = (chapNm.isBlank() ? "" : chapNm)
                        + (secNm.isBlank() ? "" : " - " + secNm)
                        + (artName.isBlank() ? "" : " - " + artName);

                String content = String.valueOf(src.getOrDefault("content", ""));
                String detail  = truncate(content, 400);

                Map<String, String> m = new HashMap<>();
                m.put("id", id);
                m.put("title", title);
                m.put("detail", detail);
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> out = new HashMap<>();
            out.put(KEY_GUIDE_INDEX, index);
            out.put(KEY_GUIDE_HITS,  guideHits);
            return CompletableFuture.completedFuture(out);

        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    DraftState.ERRORS, "[StandardRetrieverNode] " + e.getMessage()
            ));
        }
    }

    // --- helpers ---

    private String pickIndex(String label) {
        if (label == null) return "standard";
        return switch (label) {
            case "사업위험", "회사위험", "기타 투자위험" -> "risk_standard";
            default -> "standard";
        };
    }

    private List<String> pickChapIds(String label) {
        if (label == null) return List.of();
        return switch (label) {
            case "사업위험" -> List.of("5");
            case "회사위험" -> List.of("6");
            case "기타 투자위험" -> List.of("7");
            default -> List.of();
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> readTokens(DraftState state) {
        Optional<List<String>> fromSearchTerms = state.value(DraftState.SEARCH_TERMS);
        if (fromSearchTerms.isPresent() && !fromSearchTerms.get().isEmpty()) {
            return fromSearchTerms.get();
        }
        Optional<Object> maybeTokens = state.value("draftTokens");
        if (maybeTokens.isPresent()) {
            Object o = maybeTokens.get();
            if (o instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String) {
                return (List<String>) list;
            }
            if (o instanceof Map<?, ?> map && map.containsKey("tokens")) {
                Object arr = map.get("tokens");
                if (arr instanceof List<?> l) {
                    List<String> result = new ArrayList<>();
                    for (Object item : l) {
                        if (item instanceof Map<?, ?> m && m.get("token") != null) {
                            result.add(String.valueOf(m.get("token")));
                        }
                    }
                    if (!result.isEmpty()) return result;
                }
            }
        }
        String draft = state.<String>value(DraftState.DRAFT).orElse("");
        if (!draft.isBlank()) {
            return Arrays.stream(draft.split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .limit(200)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private String buildQueryString(String sectionLabel, List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        if (sectionLabel != null && !sectionLabel.isBlank()) sb.append(sectionLabel).append(' ');
        for (String t : tokens) if (t != null && !t.isBlank()) sb.append(t).append(' ');
        return sb.toString().trim();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}
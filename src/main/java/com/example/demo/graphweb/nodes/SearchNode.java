//QueryBuilderNode에서 만든 쿼리를 받아서 DuckDuckGo API 호출
// → 기사 링크를 가져와 state에 저장.
// 실제 검색
// 단순히 기사만 가져오는 것.
// 이 코드에서 1. date가 없을때는

package com.example.demo.graphweb.nodes;

import com.example.demo.constants.KeywordContants;
import com.example.demo.dto.SearchLLMDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.graphweb.WebState.Brief;
import com.example.demo.graphweb.WebState.KeywordBundle;
import com.example.demo.jsonschema.SearchSchemas;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchNode implements AsyncNodeAction<WebState> {

    private final PromptCatalogService catalog;
    private final ObjectMapper om;
    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        try {
            // 1) 입력 (재할당 없이 사용)
            String corpName   = state.getCorpName();
            String indutyName = state.getIndName();     // 팀 템플릿 호환을 위해 키 그대로 사용
            String section    = state.getSectionLabel();
            List<String> queries = state.getQueries();

            // 2) 섹션별 허용 키워드 & 스키마 상수 선택
            final Set<String> allowedKeywords;
            final String schema;

            // 💡 하드코딩된 문자열 대신, KeywordContants의 상수를 사용하여 분기합니다.
            // section(String)의 값을 상수로 정의된 각 case(String)와 비교합니다.
            switch (section) {
                case KeywordContants.SECTION_COMPANY -> {
                    allowedKeywords = new HashSet<>(KeywordContants.COM_KWD);
                    schema = SearchSchemas.COMPANY;
                }
                case KeywordContants.SECTION_BUSINESS -> {
                    allowedKeywords = new HashSet<>(KeywordContants.BUS_KWD);
                    schema = SearchSchemas.BUSINESS;
                }
                default -> throw new IllegalArgumentException("알 수 없는 섹션입니다: " + section);
            }

            // 3) 프롬프트 구성 (키워드는 JSON 배열 문자열로 전달)
            String keywordsJson = om.writeValueAsString(queries);
            // 💡 [오류 수정] 프롬프트 템플릿(<corp_name>)과 변수명(corp_name)을 정확히 일치시킵니다.
            Map<String, Object> vars = Map.of(
                    "corp_name",     corpName,
                    "induty_name",   indutyName,
                    "section_label", section,
                    "keywords",      keywordsJson // 템플릿의 <keywords>에 매핑됩니다.
            );

            Prompt sys  = catalog.createSystemPrompt("search_node_rule", Map.of());
            Prompt user = catalog.createPrompt("search_node_request", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());

            // 4) JSON Schema 강제 (섹션별 enum이 반영된 상수 사용)
            ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                    .name("SearchLLMDto")
                    .schema(schema)
                    .strict(true)
                    .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .responseFormat(ResponseFormat.builder()
                            .type(ResponseFormat.Type.JSON_SCHEMA)
                            .jsonSchema(jsonSchema)
                            .build())
                    .build();

            Prompt finalPrompt = new Prompt(messages, options);

            // 5) 호출 & 구조화 파싱
            SearchLLMDto out = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(SearchLLMDto.class);

            List<SearchLLMDto.Item> raw = Optional.ofNullable(out.getArticles()).orElse(List.of());

            // 6) 섹션별 허용 키워드로 필터 → 키워드별 그룹핑
            Map<String, List<SearchLLMDto.Item>> byKeyword = raw.stream()
                    .filter(i -> allowedKeywords.contains(i.getKeyword())) // 방어막 이중화(스키마 + 코드)
                    .collect(Collectors.groupingBy(SearchLLMDto.Item::getKeyword));

            // 7) 키워드별: URL 중복 제거 → 날짜 내림차순(null 뒤로) → 상위 3건 → 내부 DTO 매핑
            List<KeywordBundle> bundles = byKeyword.entrySet().stream()
                    .map(e -> {
                        List<SearchLLMDto.Item> dedup = e.getValue().stream()
                                .collect(Collectors.collectingAndThen(
                                        Collectors.toMap(
                                                SearchLLMDto.Item::getUrl,
                                                i -> i,
                                                (a, b) -> a,               // 충돌 시 최초 항목 유지
                                                LinkedHashMap::new
                                        ),
                                        m -> new ArrayList<>(m.values())
                                ));

                        List<Brief> top3 = dedup.stream()
                                .sorted((x, y) -> compareDateDesc(x.getDate(), y.getDate()))
                                .limit(3)
                                .map(i -> new Brief(
                                        safe(i.getTitle()),
                                        safe(i.getUrl()),
                                        safe(i.getSource()),
                                        i.getDate(),     // null 허용: 정렬 시 뒤로 감
                                        null             // 검색 단계에서는 contents 비움 (FetchNode에서 채움)
                                ))
                                .collect(Collectors.toList());

                        return new KeywordBundle(e.getKey(), top3);
                    })
                    .collect(Collectors.toList());

            // 8) 상태에 저장 (ARTICLES = 키워드별 Top3 번들)
            return CompletableFuture.completedFuture(Map.of(WebState.ARTICLES, bundles));

        } catch (Exception e) {
            log.error("[SearchNode] error", e);
            return CompletableFuture.completedFuture(Map.of(
                    WebState.ERRORS, List.of("[SearchNode] " + e.getMessage())
            ));
        }
    }

    // YYYY-MM-DD 문자열 기준 내림차순, null은 뒤로
    private static int compareDateDesc(String d1, String d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;
        return d2.compareTo(d1);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}

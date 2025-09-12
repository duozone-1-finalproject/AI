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
            // 1) QueryBuilderNode가 만든 입력값 불러오기
            String corpName = state.getCorpName();
            String indutyName = state.getIndName();
            String section = state.getSectionLabel();
            List<String> queries = state.getQueries();

            // 2) 섹션별 허용 키워드 & JSON Schema 선택
            final Set<String> allowedKeywords;
            final String schema;
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
            log.info("[SearchNode] schema: {}", schema);
            // 3) 프롬프트 구성 (system + user)
            String keywordsJson = om.writeValueAsString(queries);
            log.info("keywordsJson: {}", keywordsJson);
            Map<String, Object> vars = Map.of(
                    "corp_name", corpName,
                    "induty_name", indutyName,
                    "section_label", section,
                    "keywords", keywordsJson
            );

            Prompt sys = catalog.createSystemPrompt("search_node_rule", Map.of());
            Prompt user = catalog.createPrompt("search_node_request", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());

            // 4) JSON Schema 강제 옵션 설정
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

            // 5) LLM 호출 & 원본 응답 로깅
            SearchLLMDto out = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(SearchLLMDto.class);
            log.info("[SearchNode] LLM Raw Response: {}", out);

            // 6) DTO로 파싱
//            SearchLLMDto out = om.readValue(rawJsonResponse, SearchLLMDto.class);
//            List<SearchLLMDto.Item> raw = Optional.ofNullable(out.getCandidates()).orElse(List.of());
//
//            // 7) 키워드별 그룹핑 → 중복 제거 → null date 제거 → 최신순 정렬 → 상위 3건
//            Map<String, List<SearchLLMDto.Item>> byKeyword = raw.stream()
//                    .filter(i -> allowedKeywords.contains(i.getKeyword()))
//                    .collect(Collectors.groupingBy(SearchLLMDto.Item::getKeyword));
//
//            List<KeywordBundle> bundles = byKeyword.entrySet().stream()
//                    .map(e -> {
//                        // URL 기준 중복 제거
//                        List<SearchLLMDto.Item> dedup = e.getValue().stream()
//                                .collect(Collectors.collectingAndThen(
//                                        Collectors.toMap(
//                                                SearchLLMDto.Item::getUrl,
//                                                i -> i,
//                                                (a, b) -> a,
//                                                LinkedHashMap::new
//                                        ),
//                                        m -> new ArrayList<>(m.values())
//                                ));
//
//                        // 최신순 정렬 + 상위 3건
//                        List<Brief> top3 = dedup.stream()
//                                .filter(i -> i.getDate() != null && !i.getDate().isBlank())
//                                .sorted((x, y) -> compareDateDesc(x.getDate(), y.getDate()))
//                                .limit(3)
//                                .map(i -> new Brief(
//                                        safe(i.getTitle()),
//                                        safe(i.getUrl()),
//                                        safe(i.getSource()),
//                                        i.getDate(),
//                                        null // 검색 단계에서는 contents 비움 (FetchNode에서 채움)
//                                ))
//                                .toList();
//
//                        return new KeywordBundle(e.getKey(), top3);
//                    })
//                    .toList();

            // 8) WebState.ARTICLES에 저장
            return CompletableFuture.completedFuture(Map.of(WebState.ARTICLES, out));

        } catch (Exception e) {
            log.error("[SearchNode] error", e);
            return CompletableFuture.completedFuture(Map.of(
                    WebState.ERRORS, List.of("[SearchNode] " + e.getMessage())
            ));
        }
    }

    // 날짜 내림차순 정렬용
    private static int compareDateDesc(String d1, String d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;
        return d2.compareTo(d1);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

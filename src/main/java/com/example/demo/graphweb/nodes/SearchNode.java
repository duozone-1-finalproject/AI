//QueryBuilderNode에서 만든 쿼리를 받아서 DuckDuckGo API 호출
// → 기사 링크를 가져와 state에 저장.
// 실제 검색
// 단순히 기사만 가져오는 것.
// 이 코드에서 1. date가 없을때는

package com.example.demo.graphweb.nodes;

import com.example.demo.constants.KeywordContants;
import com.example.demo.dto.SearchLLMDto;
import com.example.demo.graphweb.WebState;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
            
            Prompt finalPrompt = new Prompt(messages);


            // 5) LLM 호출 & 원본 응답 로깅
            // 💡 [수정] ParameterizedTypeReference를 사용하여 '객체의 리스트'를 한 번에 받아옵니다.
            List<SearchLLMDto> results = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<SearchLLMDto>>() {});
            log.info("[SearchNode] results: {}", results);

            // 💡 [수정] 이제 복잡한 후처리 없이, LLM의 결과를 그대로 상태에 저장합니다.
            return CompletableFuture.completedFuture(Map.of(WebState.ARTICLES, results));

        } catch (Exception e) {
            log.error("[SearchNode] error", e);
            return CompletableFuture.completedFuture(Map.of(
                    WebState.ERRORS, List.of("[SearchNode] " + e.getMessage())
            ));
        }
    }
}

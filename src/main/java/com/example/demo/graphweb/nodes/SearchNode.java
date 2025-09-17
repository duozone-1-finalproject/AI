package com.example.demo.graphweb.nodes;

import com.example.demo.constants.WebConstants;
import com.example.demo.dto.graphweb.SearchEnvelope;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
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
            List<String> queries = state.getQueries();

            // JSON 배열 문자열로 직렬화
            String keywordsJson = om.writeValueAsString(queries);
            String domainsJson  = om.writeValueAsString(WebConstants.DOMAINS);

            Map<String, Object> vars = Map.of(
                    "keywords", keywordsJson,
                    "maxItems", 2,
                    "domains", domainsJson
            );

            Prompt sys = catalog.createSystemPrompt("search_sys", vars);
            Prompt user = catalog.createPrompt("search_user", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());

            // (3) JSON Schema 강제 (strict) 옵션 설정
            ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                    .name("SearchEnvelope")
                    .schema(WebConstants.SEARCH_JSON_SCHEMA)
                    .strict(true)
                    .build();

            ResponseFormat rf = ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(jsonSchema)
                    .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .responseFormat(rf)
                    .build();
            
            Prompt finalPrompt = new Prompt(messages, options);

            // 5) LLM 호출 & 원본 응답 로깅
            SearchEnvelope results = chatClient
                    .prompt(finalPrompt)
                    .call()
                    .entity(SearchEnvelope.class);
            log.info("[SearchNode] results: {}", results.getItems());

            // 💡 [수정] 이제 복잡한 후처리 없이, LLM의 결과를 그대로 상태에 저장합니다.
            return CompletableFuture.completedFuture(Map.of(WebState.ARTICLES, results.getItems()));

        } catch (Exception e) {
            log.error("[SearchNode] error", e);
            return CompletableFuture.completedFuture(Map.of(
                    WebState.ERRORS, List.of("[SearchNode] " + e.getMessage())
            ));
        }
    }
}

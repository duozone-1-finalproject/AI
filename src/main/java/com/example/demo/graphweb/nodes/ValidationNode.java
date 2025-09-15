package com.example.demo.graphweb.nodes;

import com.example.demo.dto.SearchLLMDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        try {
            // 1) 단건 본문(content) 확보 (String)
            String content = state.getFetchedArticles();
            if (content == null || content.isBlank()) {
                log.info("[ValidationNode] Empty fetched content -> validated=false");
                return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, false));
            }

            // 2) 제목(title) 확보: Search 단계에서 고른 후보 메타 사용 (없으면 임시 제목)
            SearchLLMDto.Item meta = state.getPickedArticle();
            String title = (meta != null && meta.getTitle() != null && !meta.getTitle().isBlank())
                    ? meta.getTitle()
                    : "untitled";

            // 3) LLM 입력 articles_json: 단건도 배열 형태로 전달
            List<Map<String, Object>> payload = List.of(Map.of(
                    "title", title,
                    "content", content
            ));
            String articlesJson = om.writeValueAsString(payload);

            // 4) 의도/주제 판단 기준이 되는 query 바인딩 (현재 키워드 > 첫 번째 원 쿼리)
            String query;
            String cur = state.getCurKeyword();
            if (cur != null && !cur.isBlank()) {
                query = cur;
            } else {
                var qs = state.getQueries();
                query = (qs != null && !qs.isEmpty()) ? qs.get(0) : "";
            }

            // 5) 프롬프트 생성
            Prompt sys = catalog.createSystemPrompt("web_validator_sys", Map.of());
            Prompt usr = catalog.createPrompt("web_validator_user", Map.of(
                    "query", query,
                    "articles_json", articlesJson
            ));

//            // 6) JSON 객체만 받도록 강제
//            var options = OpenAiChatOptions.builder()
//                    .withResponseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT))
//                    .build();

            // 7) 호출
            List<Message> msgs = new ArrayList<>(sys.getInstructions());
            msgs.addAll(usr.getInstructions());
            
            ChatOptions options = null;
            String raw = chatClient.prompt(new Prompt(msgs)).options(options).call().content();
            String json = raw.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            // 8) 응답에서 final.validated만 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> root = om.readValue(json, Map.class);

            boolean validated = false;
            Object finalObj = root.get("final"); // { "final": { "validated": true|false, ... } }
            if (finalObj instanceof Map<?, ?> fin) {
                Object v = ((Map<?, ?>) fin).get("validated");
                if (v instanceof Boolean b) {
                    validated = b;
                } else if (v != null) {
                    validated = Boolean.parseBoolean(String.valueOf(v));
                }
            }

            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, validated));

        } catch (Exception e) {
            log.error("[ValidationNode] 검증 실패 - 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.VALIDATED, false));
        }
    }
}


/*package com.example.demo.graphweb.nodes;

import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        // 1. FetchNode가 처리한 '본문이 채워진' 기사 목록을 가져옵니다.
        String fetchedArticles = state.getFetchedArticles();

        if (fetchedArticles.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        try {
            // 2. LLM에게 전달할 기사 목록을 JSON 문자열로 변환합니다.
            String articlesJson = om.writeValueAsString(fetchedArticles);

            // 3. PromptCatalogService를 사용하여 프롬프트를 생성합니다.
            Prompt sysPrompt = catalog.createSystemPrompt("web_validator_sys", Map.of());
            Prompt userPrompt = catalog.createPrompt("web_validator_user", Map.of("articles_json", articlesJson));

            List<Message> messages = new ArrayList<>(sysPrompt.getInstructions());
            messages.addAll(userPrompt.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            // 4. LLM을 호출하여 검증을 요청하고, 통과된 기사 목록(JSON)을 받습니다.
            log.info("[ValidationNode] LLM에게 기사 검증을 요청합니다.");
            String jsonResponse = chatClient.prompt(finalPrompt).call().content();
            jsonResponse = jsonResponse.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            // 5. 응답받은 JSON을 파싱하여 최종 결과 리스트를 만듭니다.
            List<WebResponseDto.Article> validatedArticles = om.readValue(jsonResponse, new TypeReference<>() {});
            log.info("[ValidationNode] 검증을 통과한 {}개의 최종 기사를 확정했습니다.", validatedArticles.size());

            // 6. 최종 결과를 'FINAL_RESULT' 상태에 저장합니다.
            return CompletableFuture.completedFuture(Map.of(WebState.FINAL_RESULT, validatedArticles));
        } catch (Exception e) {
            log.error("[ValidationNode] 기사 검증 중 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.ERRORS, List.of("[ValidationNode] " + e.getMessage())));
        }
    }
}
*/


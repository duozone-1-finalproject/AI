package com.example.demo.graphweb.nodes;

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

//@Component//
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
            Prompt sysPrompt = catalog.createSystemPrompt("web_Validator_rule", Map.of());
            Prompt userPrompt = catalog.createPrompt("web_Validator_request", Map.of("articles_json", articlesJson));

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

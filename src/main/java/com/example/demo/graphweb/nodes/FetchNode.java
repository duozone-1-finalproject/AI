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

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchNode implements AsyncNodeAction<WebState> {

    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        // 1. SearchNode의 결과(키워드별 기사 묶음)를 가져옵니다.
        List<WebState.KeywordBundle> articleBundles = state.getArticles();
        log.info("[FetchNode] SearchNode로부터 {}개의 키워드 번들을 받았습니다.", articleBundles.size());

        if (articleBundles.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        try {
            // 2. [구조 수정] 프롬프트 템플릿이 요구하는 `tasks_json` 형식에 맞게 데이터를 가공합니다.
            // 형식: [{ "keyword": "...", "urls": ["url1", "url2", ...] }, ...]
            List<Map<String, Object>> tasks = articleBundles.stream()
                    .map(bundle -> {
                        List<String> urls = bundle.searched_data().stream()
                                .map(WebState.Brief::url)
                                .toList();
                        return Map.<String, Object>of("keyword", bundle.keyword(), "urls", urls);
                    })
                    .toList();
            String tasksJson = om.writeValueAsString(tasks);

            // 3. PromptCatalogService를 사용하여 프롬프트를 동적으로 생성합니다.
            Prompt sysPrompt = catalog.createSystemPrompt("fetch_rule", Map.of());
            Prompt userPrompt = catalog.createPrompt("fetch_request", Map.of(
                    "tasks_json", tasksJson
                    // per_keyword, max_len 등은 yml의 defaults 값으로 자동 주입됩니다.
            ));
            List<Message> messages = new ArrayList<>(sysPrompt.getInstructions());
            messages.addAll(userPrompt.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            // 4. LLM을 호출하여 본문 수집을 요청합니다.
            log.info("[FetchNode] {}개의 키워드에 대한 본문 수집을 LLM에 요청합니다.", tasks.size());
            String jsonResponse = chatClient.prompt(finalPrompt).call().content();
            jsonResponse = jsonResponse.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            // 5. LLM의 응답(JSON)을 DTO 리스트로 파싱합니다.
            List<WebResponseDto.Article> fetchedArticles = om.readValue(jsonResponse, new TypeReference<>() {});

            log.info("[FetchNode] LLM으로부터 {}개의 기사 본문을 성공적으로 수집했습니다.", fetchedArticles.size());

            // 6. [상태 저장 수정] 결과를 'FETCHED_ARTICLES' 상태에 저장하여 다음 노드로 전달합니다.
            return CompletableFuture.completedFuture(Map.of(WebState.FETCHED_ARTICLES, fetchedArticles));
        } catch (Exception e) {
            log.error("[FetchNode] 본문 수집 중 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.ERRORS, List.of("[FetchNode] " + e.getMessage())));
        }
    }
}

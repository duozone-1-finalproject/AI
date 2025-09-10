//QueryBuilderNode에서 만든 쿼리를 받아서 DuckDuckGo API 호출
// → 기사 링크를 가져와 state에 저장.
// 실제 검색

package com.example.demo.graphweb.nodes;

import com.example.demo.dto.WebResponseDto;
import com.example.demo.service.PromptCatalogService;
import com.example.demo.graphweb.WebState;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            String corpName = state.getCorpName();
            String indutyName = state.getIndName();
            String section = state.getSectionLabel();
            // QueryBuilderNode에서 만들어둔 완성 쿼리 리스트 가져오기
            List<String> query = state.getQueries();

            Map<String, Object> vars = new HashMap<>();
            vars.put("corp_name", corpName);
            vars.put("induty_name", indutyName);
            vars.put("keywords", query);
            vars.put("section_label", section);

//            log.info("프롬프트 vars: {}", vars);
            // 시스템/유저 프롬프트 구성
            Prompt sys = catalog.createSystemPrompt("web_rule", Map.of());
            Prompt user = catalog.createPrompt("web_search", vars);

            List<Message> messages = new ArrayList<>(sys.getInstructions());
            messages.addAll(user.getInstructions());
            Prompt finalPrompt = new Prompt(messages);

            //LLM 호출 및 JSON 파싱
            String json = chatClient.prompt(finalPrompt).call().content();
            log.info("Search노드 응답 json: {}", json);

            json = json.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            WebResponseDto wr = om.readValue(json, WebResponseDto.class);
            var acl = (wr.getArticles() == null ? List.<WebResponseDto.Article>of() : wr.getArticles())
                    .stream().map(i -> Map.of(
                            "keyword", nvl(i.getKeyword()),
                            "title", nvl(i.getTitle()),
                            "url", nvl(i.getUrl()),
                            "date", nvl(i.getDate()),
                            "source", nvl(i.getSource())
                    )).toList();

            return CompletableFuture.completedFuture(Map.of(WebState.ARTICLES, acl));

        } catch (Exception e) {
            return CompletableFuture.completedFuture(Map.of(
                    WebState.ERRORS, List.of("[SearchNode] " + e.getMessage())
            ));
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
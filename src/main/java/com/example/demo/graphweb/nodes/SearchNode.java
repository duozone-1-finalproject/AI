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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
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
//            System.out.println("시작 !!!!!!!!");
//            Prompt sys = catalog.createSystemPrompt("searchnode_rule", Map.of());
//            System.out.println("sys !!!!!!!!" + sys);
//            Prompt user = catalog.createPrompt("searchnode_request", vars);
//            System.out.println("user !!!!!!!!" + user);
//
//            List<Message> messages = new ArrayList<>(sys.getInstructions());
//            messages.addAll(user.getInstructions());
//            Prompt finalPrompt = new Prompt(messages);
//            System.out.println("finalPrompt  !!!!!!!!!!!!!!" + finalPrompt);



            List<Message> messages = new ArrayList<>();

            // System Prompt (검색 규칙, 출력 규칙 등)
            messages.add(new SystemMessage(
                    "당신은 “키워드 웹 검색 수집기”입니다.\n\n" +
                            "[도구 제약]\n" +
                            "- 오직 MCP 도구 \\\"search\\\"만 호출합니다. (\\\"fetch_content\\\" 등 다른 도구 금지)\n\n" +
                            "[출력 형식]\n" +
                            "- 반드시 최상위 JSON 객체에 `articles` 배열을 포함해야 합니다.\n" +
                            "- JSON 외 텍스트/마크다운/코드블록 금지.\n" +
                            "- `articles` 배열의 각 항목 스키마:\n" +
                            "{\\n" +
                            "  \\\"keyword\\\": \\\"원본 키워드\\\",\\n" +
                            "  \\\"section_label\\\": \\\"사업위험 또는 회사위험\\\",\\n" +
                            "  \\\"title\\\": \\\"제목\\\",\\n" +
                            "  \\\"url\\\": \\\"정규화된 URL\\\",\\n" +
                            "  \\\"date\\\": \\\"YYYY-MM-DD 또는 null\\\",\\n" +
                            "  \\\"source\\\": \\\"뉴스\\\"\\n" +
                            "}\\n" +
                            "- 배열 길이 = (키워드 수 × 3)\n\n" +
                            "[검색/선정 규칙]\n" +
                            "- 사용자 입력 키워드를 그대로 쿼리로 사용(번역/동의어/필터/사이트 제한 추가 금지).\n" +
                            "- 각 항목에 section_label을 반드시 포함.\n" +
                            "- 키워드마다 충분한 후보를 수집 후, 최신성·적합도 기준 상위 3개만 선별.\n" +
                            "- 동일 기사/포스트의 중복 URL은 1건만 남기고 제거(AMP/모바일/추적 파라미터 등 변형 포함).\n" +
                            "- 결과는 입력 키워드 순서를 유지하며, 각 항목에 \\\"검색keyword\\\"로 원 키워드를 표기.\n\n" +
                            "[날짜]\n" +
                            "- 가능한 경우 게시/업데이트 일자를 YYYY-MM-DD 형식으로 통일.\n" +
                            "- 불명확하면 반드시 null.\n\n" +
                            "[출처 분류]\n" +
                            "- URL 도메인을 분석하여 [\\\"뉴스\\\"] 하나만 표기"
            ));

            // User Prompt (입력 값 전달)
            messages.add(new UserMessage(
                    "[입력]\n" +
                            String.format("- 회사명(CORP_NAME): %s\n", corpName) +
                            String.format("- 산업명(IND_NAME): %s\n", indutyName) +
                            String.format("- 검색 키워드 리스트(KEYWORD_LIST): %s\n", query) +
                            String.format("- 섹션 구분(SECTION_LABEL): %s\n", section) +
                            "- 키워드당 최대 결과 수(MAX_ITEMS): 3\n\n" +
                            "[작업 지시]\n" +
                            "- 각 키워드를 그대로 사용해 MCP `search` 도구를 호출한다.\n" +
                            "- URL 중복을 제거하고 상위 3개만 남긴다.\n" +
                            "- 각 항목에서 title, url, date, source, section_label을 추출한다.\n\n" +
                            "[최종 출력 JSON 스키마]\n" +
                            "{\n" +
                            "  \\\"articles\\\": [\n" +
                            "    {\n" +
                            "      \\\"keyword\\\": \\\"원본 키워드\\\",\n" +
                            "      \\\"section_label\\\": \\\"사업위험 또는 회사위험\\\",\n" +
                            "      \\\"title\\\": \\\"제목\\\",\n" +
                            "      \\\"url\\\": \\\"https://example.com/news/123\\\",\n" +
                            "      \\\"date\\\": \\\"YYYY-MM-DD 또는 null\\\",\n" +
                            "      \\\"source\\\": \\\"뉴스\\\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"
            ));
            Prompt finalPrompt = new Prompt(messages);




            //LLM 호출 및 JSON 파싱
            String json = chatClient.prompt(finalPrompt).call().content();
//            log.info("Search노드 응답 json: {}", json);

            json = json.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            WebResponseDto wr = om.readValue(json, WebResponseDto.class);
            List<WebResponseDto.Article> acl =
                    (wr.getArticles() == null ? List.of() : wr.getArticles());

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
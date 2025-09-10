// 키워드 별로 3개씩 자료를 찾고, 가장 최신순의 자료를 기준으로 1개만 크롤링함.
// 그렇게 크롤링된 결과 값은 mcp fetch를 통해 본문 긁어옴
// 그렇게 긁어온 본문은 article이라는 state에 저장되고 검증 시작.

// 키워드 별로 3개씩 자료를 찾고, 가장 최신순의 자료를 기준으로 1개만 크롤링함.
// 그렇게 크롤링된 결과 값은 mcp fetch를 통해 본문 긁어옴
// 그렇게 긁어온 본문은 article이라는 state에 저장되고 검증 시작.
package com.example.demo.graphweb.nodes;

import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchNode implements AsyncNodeAction<WebState> {

    private final ChatClient chatWithMcp;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        List<WebResponseDto.Article> articles = state.getArticles();

        if (articles.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 1. [규칙 적용] 날짜가 있고, 키워드가 있는 유효한 기사만 필터링
        // 2. [규칙 적용] 키워드별로 그룹화하여 최신순 상위 3개만 선택
        List<WebResponseDto.Article> articlesToFetch = articles.stream()
                .filter(article -> article.getKeyword() != null && !article.getKeyword().isBlank())
                .filter(article -> article.getDate() != null && !article.getDate().isBlank())
                .collect(Collectors.groupingBy(
                        WebResponseDto.Article::getKeyword))
                .values().stream()
                .flatMap(group -> group.stream()
                        .sorted(Comparator.comparing(WebResponseDto.Article::getDate).reversed())
                        .limit(1)) // 키워드 별로 3개씩 자료를 찾고, 가장 최신순의 자료를 기준으로 1개만 크롤링함
                .toList();

        if (articlesToFetch.isEmpty()) {
            log.warn("[FetchNode] 처리할 유효한 기사가 없습니다.");
            return CompletableFuture.completedFuture(Map.of());
        }

        try {
            // 3. [성능 개선] 개별 호출 대신, 전체 목록을 한번에 LLM에게 전달하여 처리 요청
            String tasksJson = om.writeValueAsString(articlesToFetch.stream()
                    .map(a -> Map.of("keyword", a.getKeyword(), "url", a.getUrl(), "title", a.getTitle(), "date", a.getDate(), "source", a.getSource()))
                    .toList());

            // Prompt를 .st 파일로 분리하는 것이 가장 이상적이지만, 우선 하드코딩으로 구현
            String sysText = "당신은 “웹 본문 수집 전문가”입니다. 입력받은 JSON 배열의 각 항목에 대해 `fetch_content` 도구를 호출하여 본문을 가져온 후, 입력된 모든 필드와 `content` 필드를 포함한 완전한 JSON 객체들의 배열을 반환해야 합니다. 출력은 오직 JSON 배열이어야 합니다.";

            String userText = "[작업 대상 목록]\n" + tasksJson;

            /*
            // 하드코딩된 System/User Prompt
            String sysText = "당신은 “웹 본문 요약·정규화 수집기”입니다.\n"
                    + "[도구 사용 제약]\n"
                    + "- 오직 MCP 도구 \"fetch_content\"만 호출합니다. (search 및 그 외 도구 호출 금지)\n"
                    + "[입력/출력 규칙]\n"
                    + "- 출력은 오직 JSON 배열만 허용합니다. JSON 외 텍스트/마크다운/코드블록 금지.\n"
                    + "- 각 아이템은 스키마 { \"keyword\",\"title\",\"url\",\"source\",\"date\",\"text\" } 를 가집니다.\n"
                    + "- 본문은 정규화된 키워드 어절만 남깁니다.\n";


            String userText = "[작업 입력]\n"
                    + "- keyword: " + article.getKeyword() + "\n"
                    + "- url: " + article.getUrl() + "\n"
                    + "- per_keyword: 2\n"
                    + "- max_len: 300\n"
                    + "- normalize: lower\n"
                    + "- joiner: _\n";
                       */

            Prompt finalPrompt = new Prompt(List.of(
                    new SystemMessage(sysText),
                    new UserMessage(userText)
            ));

            log.info("[FetchNode] {}개의 기사에 대한 본문 수집을 LLM에 요청합니다.", articlesToFetch.size());
            String jsonResponse = chatWithMcp.prompt(finalPrompt).call().content();
            jsonResponse = jsonResponse.replaceAll("```json\\s*", "").replaceAll("```", "").trim();

            // 4. [버그 수정] LLM의 응답 전체를 List<Article>로 파싱하여 title 등 모든 필드를 가져옴
            List<WebResponseDto.Article> fetchedArticles = om.readValue(jsonResponse, new TypeReference<>() {});

            log.info("[FetchNode] LLM으로부터 {}개의 기사 본문을 성공적으로 수집했습니다.", fetchedArticles.size());
            return CompletableFuture.completedFuture(Map.of(WebState.BEFOREV, fetchedArticles));
        } catch (Exception e) {
            log.error("[FetchNode] 본문 수집 중 오류 발생", e);
            return CompletableFuture.completedFuture(Map.of(WebState.ERRORS, List.of("[FetchNode] " + e.getMessage())));
        }
    }
}

                    // system / user 프롬프트 분리해서 사용
                    /*
                    Prompt sys = catalog.createSystemPrompt("fetch_rule", Map.of());
                    Prompt user = catalog.createPrompt("fetch_request", Map.of("url", article.getUrl()));


                    List<Message> messages = new ArrayList<>(sys.getInstructions());
                    messages.addAll(user.getInstructions());
                    Prompt finalPrompt = new Prompt(messages);


                    String json = chatWithMcp.prompt(finalPrompt).call().content();

                    // JSON 파싱 후 본문 content 추출
                    JsonNode node = om.readTree(json);
                    String content = node.has("content") ? node.get("content").asText() : "";


                    copy.setContent(content);
                    beforevList.add(copy);


                    System.out.println("[FetchNode] 본문 크롤링 완료: " + copy.getUrl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }


        return CompletableFuture.completedFuture(Map.of(WebState.BEFOREV, beforevList));
    }
}
*/





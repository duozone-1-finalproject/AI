// 키워드 별로 3개씩 자료를 찾고, 가장 최신순의 자료를 기준으로 1개만 크롤링함.
// 그렇게 크롤링된 결과 값은 mcp fetch를 통해 본문 긁어옴
// 그렇게 긁어온 본문은 article이라는 state에 저장되고 검증 시작.

package com.example.demo.graphweb.nodes;

import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.PromptCatalogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FetchNode implements AsyncNodeAction<WebState> {

    private final ChatClient chatWithMcp;
    private final PromptCatalogService catalog;
    private final ObjectMapper om;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        List<WebResponseDto.Article> articles = state.getArticles();
        System.out.println("articles" + articles);

        if (articles.isEmpty()) {
            System.out.println("여기까지는 괜찮음1");
            return CompletableFuture.completedFuture(Map.of());
        }
        System.out.println("여기까지는 괜찮음2");
        // 키워드별로 최신 기사 1개 선택
        Map<String, Optional<WebResponseDto.Article>> latestByKeyword = articles.stream()
                .collect(Collectors.groupingBy(
                        WebResponseDto.Article::getKeyword,
                        Collectors.maxBy(
                                Comparator.comparing(WebResponseDto.Article::getDate,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                        )
                ));
        System.out.println("여기까지는 괜찮음3");
        // BEFOREV에 저장할 리스트
        List<WebResponseDto.Article> beforevList = new ArrayList<>();

        for (Optional<WebResponseDto.Article> opt : latestByKeyword.values()) {
            opt.ifPresent(article -> {
                try {
                // 원본 ARTICLES는 그대로 두고 복사본 생성
                WebResponseDto.Article copy = new WebResponseDto.Article();
                copy.setKeyword(article.getKeyword());
                copy.setSectionLabel(article.getSectionLabel());
                copy.setTitle(article.getTitle());
                copy.setUrl(article.getUrl());
                copy.setDate(article.getDate());
                copy.setSource(article.getSource());

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


                    Prompt finalPrompt = new Prompt(List.of(
                            new SystemMessage(sysText),
                            new UserMessage(userText)
                    ));


                    String json = chatWithMcp.prompt(finalPrompt).call().content();
                    System.out.println("json !!!!"+ json);


                    JsonNode node = om.readTree(json);
                    System.out.println("node !!!!"+ node);
                    String content = node.isArray() && node.get(0).has("text") ? node.get(0).get("text").asText() : "";


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





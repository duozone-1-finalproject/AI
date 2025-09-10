// 키워드 별로 3개씩 자료를 찾고, 가장 최신순의 자료를 기준으로 1개만 크롤링함.
// 그렇게 크롤링된 결과 값은 mcp fetch를 통해 본문 긁어옴
// 그렇게 긁어온 본문은 article이라는 state에 저장되고 검증 시작.

package com.example.demo.graphweb.nodes;

import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FetchNode implements AsyncNodeAction<WebState> {

    private final ChatClient chatWithMcp;

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        List<WebResponseDto.Article> articles = state.getArticles();

        if (articles.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 키워드별로 최신 기사 1개 선택
        Map<String, Optional<WebResponseDto.Article>> latestByKeyword = articles.stream()
                .collect(Collectors.groupingBy(
                        WebResponseDto.Article::getKeyword,
                        Collectors.maxBy(Comparator.comparing(WebResponseDto.Article::getDate))
                ));
        // BEFOREV에 저장할 리스트
        List<WebResponseDto.Article> beforevList = new ArrayList<>();

        for (Optional<WebResponseDto.Article> opt : latestByKeyword.values()) {
            opt.ifPresent(article -> {
                // 원본 ARTICLES는 그대로 두고 복사본 생성
                WebResponseDto.Article copy = new WebResponseDto.Article();
                copy.setKeyword(article.getKeyword());
                copy.setSectionLabel(article.getSectionLabel());
                copy.setTitle(article.getTitle());
                copy.setUrl(article.getUrl());
                copy.setDate(article.getDate());
                copy.setSource(article.getSource());

        // 각 최신 기사에 대해 fetch 실행 -> 이거 tool이 아니라 prompt에서 불러올 수 있도록 설정.
                String content = chatWithMcp.prompt(p -> p
                                .user("URL 본문 크롤링: " + article.getUrl())
                                .options(o -> o.withTool("fetch")))
                        .call().content();

                copy.setContent(content);
                beforevList.add(copy);

                return CompletableFuture.completedFuture(Map.of(WebState.BEFOREV, beforevList));
    }
}



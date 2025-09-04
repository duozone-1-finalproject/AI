// ✅ SummaryNode 뼈대
package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.Node;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SummaryNode implements Node<WebState> {

    @Override
    public CompletableFuture<WebState> apply(WebState state) {
        // 기사 리스트 가져오기
        List<String> articles = state.value(WebState.ARTICLES).orElse(List.of());
        List<String> summaries = new ArrayList<>();

        // 간단한 더미 요약 로직 (실제 구현은 LLM 호출)
        for (String article : articles) {
            String summary = article.substring(0, Math.min(article.length(), 100));
            summaries.add(summary);
            System.out.println("[SummaryNode] 요약 생성: " + summary);
        }

        // state에 요약 저장
        state.set(WebState.SUMMARIES, summaries);

        return CompletableFuture.completedFuture(state);
    }
}

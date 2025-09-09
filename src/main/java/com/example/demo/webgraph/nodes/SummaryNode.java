//외부에서 가져온 데이터(webDocs, newsDocs) 만 요약 대상

package com.example.demo.webgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.webgraph.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SummaryNode implements AsyncNodeAction<WebState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(WebState state) {
        // 외부 데이터만 요약 대상으로 사용 (web + news)
        List<String> webDocs = state.getWebDocs();

        List<String> summaries = new ArrayList<>();

        // 간단한 더미 요약 로직 (실제 구현은 LLM 호출 예정)
        for (Object doc : webDocs) {
            String text = doc.toString();
            String summary = text.substring(0, Math.min(text.length(), 100));
            summaries.add(summary);
        }

        // state 업데이트
        Map<String, Object> partial = Map.of(
                WebState.SUMMARIES, summaries
        );

        System.out.println("[SummaryNode] 생성된 요약 개수: " + summaries.size());

        return CompletableFuture.completedFuture(partial);
    }
}

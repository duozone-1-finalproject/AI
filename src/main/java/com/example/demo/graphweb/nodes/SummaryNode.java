//외부에서 가져온 데이터(webDocs, newsDocs) 만 요약 대상

package com.example.demo.graphweb.nodes;

import com.example.demo.graphmain.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SummaryNode implements AsyncNodeAction<DraftState> {

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        // 외부 데이터만 요약 대상으로 사용 (web + news)
        List<?> webDocs = state.webDocs();
        List<?> newsDocs = state.newsDocs();

        List<String> summaries = new ArrayList<>();

        // 간단한 더미 요약 로직 (실제 구현은 LLM 호출 예정)
        for (Object doc : webDocs) {
            String text = doc.toString();
            String summary = text.substring(0, Math.min(text.length(), 100));
            summaries.add(summary);
        }
        for (Object doc : newsDocs) {
            String text = doc.toString();
            String summary = text.substring(0, Math.min(text.length(), 100));
            summaries.add(summary);
        }

        // state 업데이트
        Map<String, Object> partial = Map.of(
                DraftState.SUMMARIES, summaries
        );

        System.out.println("[SummaryNode] 생성된 요약 개수: " + summaries.size());

        return CompletableFuture.completedFuture(partial);
    }
}

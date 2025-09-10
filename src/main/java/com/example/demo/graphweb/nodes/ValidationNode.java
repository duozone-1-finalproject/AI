/*package com.example.demo.langgraph.web.nodes;

import com.example.demo.webgraph.state.web.WebState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ValidationNode implements AsyncNodeAction<WebState> {

    @Override
    public CompletableFuture<Map<String, Object> apply(WebState/* state) {
        List<String> summaries = (List<String>) state.value(WebState.SUMMARIES).orElse(List.of());

        boolean validated = true;
        for (String summary : summaries) {
            // 단순 규칙 기반 검증 (실제 구현은 LLM judge + 원문 스니펫 비교)
            if (summary == null || summary.isBlank()) {
                validated = false;
                break;
            }
        }

        state.set(WebState.VALIDATED, validated);
        System.out.println("[ValidationNode] 검증 결과: " + validated);

        return CompletableFuture.completedFuture((Map<String, Object>) state);
    }
}

*/
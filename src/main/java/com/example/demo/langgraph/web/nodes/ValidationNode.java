package com.example.demo.langgraph.web.nodes;

import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.Node;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ValidationNode implements Node<WebState> {

    @Override
    public CompletableFuture<WebState> apply(WebState state) {
        List<String> summaries = state.value(WebState.SUMMARIES).orElse(List.of());

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

        return CompletableFuture.completedFuture(state);
    }
}



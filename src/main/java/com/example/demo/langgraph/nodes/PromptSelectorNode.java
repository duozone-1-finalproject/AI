package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.PromptCatalogService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.langgraph.state.DraftState.*;

// 프롬프트 선택 노드
@Component("prompt")
@RequiredArgsConstructor
public class PromptSelectorNode implements AsyncNodeAction<DraftState> {
    private final PromptCatalogService catalog;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String section = state.<String>value(DraftState.SECTION).orElseThrow();

        Map<String,Object> vars = Map.of(
                "corpCode",  state.<String>value(DraftState.CORP_CODE).orElse(""),
                "corpName",  state.<String>value(DraftState.CORP_NAME).orElse(""),
                "indutyCode",state.<String>value(DraftState.IND_CODE).orElse(""),
                "indutyName",state.<String>value(DraftState.IND_NAME).orElse("")
        );

        // 3) 템플릿 로드 + 치환 → prompt 문자열 생성
        //    I/O가 가볍다면 completedFuture, 무겁다면 supplyAsync로 풀어도 OK
        String prompt = catalog.renderToString(section, vars);

        // 4) 상태 업데이트 맵을 비동기로 반환
        return CompletableFuture.completedFuture(Map.of(PROMPT, prompt));
    }

}

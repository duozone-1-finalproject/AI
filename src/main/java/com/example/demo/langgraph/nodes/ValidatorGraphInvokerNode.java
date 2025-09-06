package com.example.demo.langgraph.nodes;

import com.example.demo.dto.CheckRequestDto;
import com.example.demo.langgraph.state.DraftState;
import com.example.demo.service.CheckService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 검증 그래프 호출
@Component("validatorGraphInvoker")
@RequiredArgsConstructor
public class ValidatorGraphInvokerNode implements AsyncNodeAction<DraftState> {

    private final CheckService checkService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String draft = state.<List<String>>value(DraftState.DRAFT).orElse(List.of()).getLast();
        String section = state.<String>value(DraftState.SECTION).orElse("");
        String indutyName = state.<String>value(DraftState.IND_NAME).orElse("");

        CheckRequestDto dto = new CheckRequestDto();
        dto.setDraft(draft);
        dto.setSection(section);
        dto.setIndutyName(indutyName);

        List<String> drafts = checkService.draftValidate(dto);
        return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, drafts));
    }
}


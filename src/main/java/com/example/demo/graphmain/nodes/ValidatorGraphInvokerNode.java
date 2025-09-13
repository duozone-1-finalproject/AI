package com.example.demo.graphmain.nodes;

import com.example.demo.dto.graphvalidator.CheckRequestDto;
import com.example.demo.graphmain.DraftState;
import com.example.demo.service.graphvalidator.CheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 검증 그래프 호출
@Slf4j
@Component("validatorGraphInvoker")
@RequiredArgsConstructor
public class ValidatorGraphInvokerNode implements AsyncNodeAction<DraftState> {

    private final CheckService checkService;

    @Override
    public CompletableFuture<Map<String, Object>> apply(DraftState state) {
        String draft = state.getDrafts().getLast();
        String section = state.getSection();
        String indutyName = state.getIndutyName();

        log.info("### 검증 그래프 INIT DATA ###");
        log.info("draft: {}", draft);
        log.info("section: {}", section);
        log.info("indutyName: {}", indutyName);
        log.info("### 검증 그래프 시작점 ###");



        CheckRequestDto dto = new CheckRequestDto();
        dto.setDraft(draft);
        dto.setSection(section);
        dto.setIndutyName(indutyName);

        List<String> drafts = checkService.draftValidate(dto);
        return CompletableFuture.completedFuture(Map.of(DraftState.DRAFT, drafts));
    }
}


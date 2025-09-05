package com.example.demo.service;

import com.example.demo.config.AiSectionProperties;
import com.example.demo.dto.CheckRequestDto;
import com.example.demo.dto.ValidationDto;
import com.example.demo.validatorgraph.ValidatorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckServiceImpl implements CheckService {

    private final CompiledGraph<ValidatorState> graph;
    private final AiSectionProperties aiSectionProperties;

    @Override
    public ValidationDto check(CheckRequestDto req) {
        String section = req.getSection();
        String sectionLabel = aiSectionProperties.getSections().get(section).getLabel();

        Map<String, Object> init = new LinkedHashMap<>();
        init.put(ValidatorState.METHOD, "check");
        init.put(ValidatorState.IND_NAME, req.getIndutyName());
        init.put(ValidatorState.SECTION, section);
        init.put(ValidatorState.SECTION_LABEL, sectionLabel);
        init.put(ValidatorState.DRAFT, req.getDraft());

        AsyncGenerator<NodeOutput<ValidatorState>> stream = graph.stream(init);

        final AtomicReference<ValidatorState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            ValidatorState currentState = nodeOutput.state();
            // 디버깅용 로그처리(즉시 보고싶다면, info)
            // log.info("Graph node processed. Current state: {}", currentState);
            log.debug("Graph node processed. Current ValidatorState: {}", currentState);
            finalStateRef.set(currentState);
        });

        ValidatorState finalState = finalStateRef.get();
        if (finalState == null) {
            // 스트림이 비어있는 경우에 대한 처리
            finalState = new ValidatorState(Map.of());
        }

        return finalState.<ValidationDto>value(ValidatorState.VALIDATION).orElseThrow();
    }

    @Override
    public List<String> draftValidate(CheckRequestDto req) {
        String section = req.getSection();
        String sectionLabel = aiSectionProperties.getSections().get(section).getLabel();

        Map<String, Object> init = new LinkedHashMap<>();
        init.put(ValidatorState.METHOD, "draft");
        init.put(ValidatorState.IND_NAME, req.getIndutyName());
        init.put(ValidatorState.SECTION, section);
        init.put(ValidatorState.SECTION_LABEL, sectionLabel);
        init.put(ValidatorState.DRAFT, req.getDraft());

        AsyncGenerator<NodeOutput<ValidatorState>> stream = graph.stream(init);

        final AtomicReference<ValidatorState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            ValidatorState currentState = nodeOutput.state();
            // 디버깅용 로그처리(즉시 보고싶다면, info)
            // log.info("Graph node processed. Current state: {}", currentState);
            log.debug("Graph node processed. Current ValidatorState: {}", currentState);
            finalStateRef.set(currentState);
        });

        ValidatorState finalState = finalStateRef.get();
        if (finalState == null) {
            // 스트림이 비어있는 경우에 대한 처리
            finalState = new ValidatorState(Map.of());
        }

        return finalState.<List<String>>value(ValidatorState.DRAFT).orElseThrow();
    }
}

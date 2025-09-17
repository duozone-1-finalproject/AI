package com.example.demo.service.graphvalidator.impl;

import com.example.demo.dto.graphvalidator.CheckRequestDto;
import com.example.demo.dto.graphvalidator.ValidationDto;
import com.example.demo.graphvalidator.ValidatorState;
import com.example.demo.service.graphmain.impl.PromptCatalogService;
import com.example.demo.service.graphvalidator.CheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.demo.constants.YamlConstants.SECTION_MAP;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckServiceImpl implements CheckService {

    @Qualifier("default")
    private final ChatClient chatClient;
    private final CompiledGraph<ValidatorState> graph;
    private final PromptCatalogService catalog;


    private ValidatorState runGraph(Map<String, Object> init) {
        log.debug("################################ VALIDATOR GRAPH START ({}) ##########################################", init.get(ValidatorState.SECTION_LABEL));
        AsyncGenerator<NodeOutput<ValidatorState>> stream = graph.stream(init);
        final AtomicReference<ValidatorState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            ValidatorState currentState = nodeOutput.state();

            log.debug("Validator Graph node processed. Current node: {}", nodeOutput.node());
            log.debug("ValidateState drafts(size={}): {}", currentState.getDraft().size(), currentState.getDraft());
            log.debug("{}", currentState);
            finalStateRef.set(currentState);
        });

        ValidatorState finalState = finalStateRef.get();
        log.debug("################################ VALIDATOR GRAPH END   ({}) ##########################################", init.get(ValidatorState.SECTION_LABEL));
        return (finalState != null) ? finalState : new ValidatorState(Map.of());
    }

    @Override
    public ValidationDto check(CheckRequestDto req) {
        Map<String, Object> init = new LinkedHashMap<>();
        init.put(ValidatorState.METHOD, "check");
        init.put(ValidatorState.IND_NAME, req.getIndutyName());
        init.put(ValidatorState.SECTION, req.getSection());
        init.put(ValidatorState.SECTION_LABEL, SECTION_MAP.get(req.getSection()));
        init.put(ValidatorState.DRAFT, List.of(req.getDraft()));

        ValidatorState state = runGraph(init);
        return state.getValidation();
    }

    @Override
    public List<String> draftValidate(CheckRequestDto req) {
        Map<String, Object> init = new LinkedHashMap<>();
        init.put(ValidatorState.METHOD, "draft");
        init.put(ValidatorState.IND_NAME, req.getIndutyName());
        init.put(ValidatorState.SECTION, req.getSection());
        init.put(ValidatorState.SECTION_LABEL, SECTION_MAP.get(req.getSection()));
        init.put(ValidatorState.DRAFT, List.of(req.getDraft()));

        ValidatorState state = runGraph(init);
        return state.getDraft();
    }

    @Override
    public String revise(ValidationDto.Issue req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("span", req.getSpan());
        vars.put("reason", req.getReason());
        vars.put("ruleId", req.getRuleId());
        vars.put("evidence", req.getEvidence());
        vars.put("suggestion", req.getSuggestion());
        vars.put("severity", req.getSeverity());

        Prompt sys = catalog.createSystemPrompt("check_revise_sys", Map.of());
        Prompt user = catalog.createPrompt("check_revise_user", vars);

        List<Message> reviseMsgs = new ArrayList<>(sys.getInstructions());
        reviseMsgs.addAll(user.getInstructions());

        return chatClient.prompt(new Prompt(reviseMsgs)).call().content();
    }
}

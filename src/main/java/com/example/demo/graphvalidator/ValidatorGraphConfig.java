package com.example.demo.graphvalidator;

import com.example.demo.graphvalidator.nodes.*;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Configuration
@RequiredArgsConstructor
public class ValidatorGraphConfig {

    // Node Class 선언 //
    private final StandardRetrieverNode standardRetriever;
    private final GlobalValidatorNode globalValidator;
    private final AdjustDraftNode adjustDraft;

    @Bean
    public CompiledGraph<ValidatorState> validatorGraph() throws GraphStateException {
        // State 인스턴스 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(ValidatorState.SCHEMA);
        StateGraph<ValidatorState> graph = new StateGraph<>(schema, ValidatorState::new);

        // 노드 설정(graph.addNode)
        graph.addNode("guideline", standardRetriever);
        graph.addNode("validate", globalValidator);
        graph.addNode("adjust", adjustDraft);

        // 엣지 설정(graph.addEdge)
        graph.addEdge(StateGraph.START, "guideline");
        graph.addEdge("guideline", "validate");
        // 'validate' 노드의 결정에 따라 분기합니다.
        graph.addConditionalEdges("validate",
                // DECISION 상태 값을 읽어 다음 노드를 결정합니다.
                edge_async(s -> "end".equals(s.value(ValidatorState.DECISION).orElse("")) ? StateGraph.END : "adjust"),
                Map.of(StateGraph.END, StateGraph.END, "adjust", "adjust")
        );
        // 'adjust' 노드는 다시 'validate' 노드로 연결되어 재검증 루프를 형성합니다.
        graph.addEdge("adjust", "validate");

        return graph.compile();
    }
}

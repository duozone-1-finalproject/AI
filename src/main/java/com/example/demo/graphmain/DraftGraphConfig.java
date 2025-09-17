package com.example.demo.graphmain;

import com.example.demo.graphmain.nodes.DbSubgraphInvoker;
import com.example.demo.graphmain.nodes.*;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final SourceSelectorNode sourceSelector;
    private final WebSubgraphInvoker webSubgraphInvoker; // 웹검색 RAG 서브 랭그래프
    private final DbSubgraphInvoker dbSubgraphInvoker; // DB검색 RAG 서브 랭그래프
    private final ContextAggregatorNode contextAggregator; // RAG Context 병합 노드
    private final BaseVarsInitializerNode baseVarsInitializerNode;
    private final DraftGeneratorNode draftGenerator;
    private final MinimalCheckNode minimalCheck;
    private final ValidatorGraphInvokerNode validatorInvoker;


    @Bean
    public CompiledGraph<DraftState> draftGraph() throws GraphStateException {
        // StateGraph 인스턴스 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(DraftState.SCHEMA);
        StateGraph<DraftState> graph = new StateGraph<>(schema, DraftState::new);

        // 노드정의
        graph.addNode("source_select", sourceSelector);
        // fan-out 진입 전, 빈/비빈만 판별하는 NOOP 게이트
        graph.addNode("fanout_gate", node_async(st -> Map.of())); // 불변 빈 Map (null 없음)
        graph.addNode("web_branch", webSubgraphInvoker);
        graph.addNode("db_branch", dbSubgraphInvoker);
        graph.addNode("aggregate", contextAggregator);
        graph.addNode("base_vars_init", baseVarsInitializerNode);
        graph.addNode("generate", draftGenerator);
        graph.addNode("minimal_check", minimalCheck);
        graph.addNode("validate", validatorInvoker);

        // 엣지연결
        graph.addEdge(START, "source_select");

        // 1) 가드: sources가 []/null 이면 즉시 base_vars_init, 아니면 fanout_gate
        graph.addConditionalEdges(
                "source_select",
                edge_async(s -> {
                    List<String> selected = s.getSources();
                    boolean empty = (selected == null || selected.isEmpty());
                    return empty ? "no_source" : "has_source";
                }),
                Map.of(
                        "no_source", "base_vars_init",
                        "has_source", "fanout_gate"
                )
        );

        graph.addEdge("fanout_gate", "db_branch");
        graph.addEdge("fanout_gate", "web_branch");

        // 3) 각 브랜치 → fan-in
        graph.addEdge("db_branch", "aggregate");
         graph.addEdge("web_branch", "aggregate");

        // aggregate → 조건부 엣지
        graph.addConditionalEdges("aggregate",
                edge_async(state -> {
                    boolean dbOk = state.isDbReady();
                    boolean webOk  = state.isWebReady();

                    if (!dbOk) return "db";    // DB 실패 → 다시 DB 브랜치
                    if (!webOk)  return "web";   // Web 실패 → 다시 Web 브랜치

                    return "base_init"; // 모두 성공 → base_vars_init 노드로
                }),
                Map.of(
                        "db", "db_branch",
                        "web", "web_branch",
                        "base_init", "base_vars_init"
                )
        );

        graph.addEdge("base_vars_init", "generate");
        graph.addEdge("generate", "minimal_check");
        graph.addEdge("minimal_check", "validate");
        graph.addEdge("validate", END);

        return graph.compile(); // 그래프 생성
    }
}
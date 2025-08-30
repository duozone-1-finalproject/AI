package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.*;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.*;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final PromptSelectorNode promptSelector;
    private final SourceSelectorNode sourceSelector;
    //    private final WebSubgraphInvoker webSubgraphInvoker;
//    private final NewsSubgraphInvoker newsSubgraphInvoker;
//    private final DbSubgraphInvoker dbSubgraphInvoker;
//    private final ContextAggregatorNode contextAggregator;
    private final DraftGeneratorNode draftGenerator;
    private final StandardRetrieverNode standardRetriever;
    private final GlobalValidatorNode globalValidator;
    private final AdjustDraftNode adjustDraft;


    @Bean
    public CompiledGraph<DraftState> draftGraph() throws GraphStateException {
        // StateGraph 인스턴스를 직접 생성하여 그래프 구성을 시작합니다.
        Map<String, Channel<?>> schema = new LinkedHashMap<>(DraftState.SCHEMA);
        StateGraph<DraftState> graph = new StateGraph<>(schema, DraftState::new);

        graph.addNode("prompt", promptSelector);
        graph.addNode("source_select", sourceSelector);
//                graph.addNode("web_branch",    node_async(webSubgraphInvoker));
//                graph.addNode("news_branch",   node_async(newsSubgraphInvoker));
//                graph.addNode("db_branch",     node_async(dbSubgraphInvoker));
//                graph.addNode("aggregate",     contextAggregator);
        graph.addNode("generate", draftGenerator);
        graph.addNode("guideline", standardRetriever);
        graph.addNode("validate", globalValidator);
        graph.addNode("adjust",  adjustDraft);
        graph.addEdge(StateGraph.START, "prompt");
        graph.addEdge("prompt", "source_select");
        graph.addEdge("source_select", "generate");
        graph.addEdge("generate", "guideline");
        graph.addEdge("guideline", "validate");

        // 'validate' 노드의 결정에 따라 분기합니다.
        graph.addConditionalEdges("validate",
                // DECISION 상태 값을 읽어 다음 노드를 결정합니다.
                edge_async(s -> "end".equals(s.value(DraftState.DECISION).orElse("")) ? StateGraph.END : "adjust"),
                Map.of(StateGraph.END, StateGraph.END, "adjust", "adjust")
        );

        // 'adjust' 노드는 다시 'validate' 노드로 연결되어 재검증 루프를 형성합니다.
        graph.addEdge("adjust", "validate");

        return graph.compile(); // 실행용 그래프 생성
    }
}
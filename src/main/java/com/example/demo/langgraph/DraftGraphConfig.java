package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.*;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final StateGraph<DraftState> graph;
    private final PromptSelectorNode promptSelector;
    private final SourceSelectorNode sourceSelector;
    private final WebSubgraphInvoker webSubgraphInvoker;
    private final NewsSubgraphInvoker newsSubgraphInvoker;
    private final DbSubgraphInvoker dbSubgraphInvoker;
    private final ContextAggregatorNode contextAggregator;
    private final DraftGeneratorNode draftGenerator;
    private final GlobalValidatorNode globalValidator;
    private final RetryAdjustNode retryAdjust;
    private final LlmNode llmNode;


    @Bean
    public CompiledGraph<DraftState> compiledDraftGraph() throws GraphStateException {
                graph.addNode("prompt",        promptSelector);
                graph.addNode("source_select", sourceSelector);
                graph.addNode("web_branch",    node_async(webSubgraphInvoker));
                graph.addNode("news_branch",   node_async(newsSubgraphInvoker));
                graph.addNode("db_branch",     node_async(dbSubgraphInvoker));
                graph.addNode("aggregate",     contextAggregator);
                graph.addNode("generate",      draftGenerator);
                graph.addNode("validate",      globalValidator);
                graph.addNode("retry_adjust",  retryAdjust);
                graph.addEdge(StateGraph.START, "prompt");
                graph.addEdge("prompt", "source_select");
                // 조건부 병렬 fan-out(소스별 라우팅은 CompletableFuture<String> 반환 필요)
                graph.addConditionalEdges("source_select",
                                edge_async(s -> completedFuture(s.value(DraftState.SOURCES)
                                        .orElse(List.of()).contains("web") ? "web" : "skip_web")),
                                Map.of("web","web_branch","skip_web","aggregate"));
                graph.addConditionalEdges("source_select",
                                edge_async(s -> completedFuture(s.value(DraftState.SOURCES)
                                        .orElse(List.of()).contains("news") ? "news" : "skip_news")),
                                Map.of("news","news_branch","skip_news","aggregate"));
                graph.addConditionalEdges("source_select",
                                edge_async(s -> completedFuture(s.value(DraftState.SOURCES)
                                        .orElse(List.of()).contains("db") ? "db" : "skip_db")),
                                Map.of("db","db_branch","skip_db","aggregate"));
                graph.addEdge("web_branch","aggregate");
                graph.addEdge("news_branch","aggregate");
                graph.addEdge("db_branch","aggregate");
                graph.addEdge("aggregate","generate");
                graph.addEdge("generate","validate");
                graph.addConditionalEdges("validate",
                                edge_async(s -> completedFuture(
                                        s.value(DraftState.IS_VALID).orElse(false) ? "END" : "retry_adjust")),
                                Map.of("END", StateGraph.END, "retry_adjust", "retry_adjust"));
                graph.addEdge("retry_adjust","generate");

        return graph.compile(); // 실행용 그래프 생성
    }
}
package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.*;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.*;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final StateGraph<DraftState> graph;
    private final PromptSelectorNode promptSelector;
    private final SourceSelectorNode sourceSelector;
//    private final WebSubgraphInvoker webSubgraphInvoker;
//    private final NewsSubgraphInvoker newsSubgraphInvoker;
//    private final DbSubgraphInvoker dbSubgraphInvoker;
//    private final ContextAggregatorNode contextAggregator;
    private final DraftGeneratorNode draftGenerator;
//    private final GlobalValidatorNode globalValidator;
//    private final RetryAdjustNode retryAdjust;


    @Bean
    public CompiledGraph<DraftState> draftGraph() throws GraphStateException {
                graph.addNode("prompt",        promptSelector);
                graph.addNode("source_select", sourceSelector);
//                graph.addNode("web_branch",    node_async(webSubgraphInvoker));
//                graph.addNode("news_branch",   node_async(newsSubgraphInvoker));
//                graph.addNode("db_branch",     node_async(dbSubgraphInvoker));
//                graph.addNode("aggregate",     contextAggregator);
                graph.addNode("generate",      draftGenerator);
//                graph.addNode("validate",      globalValidator);
//                graph.addNode("retry_adjust",  retryAdjust);
                graph.addEdge(StateGraph.START, "prompt");
                graph.addEdge("prompt", "source_select");
                graph.addEdge("source_select", "generate");
                graph.addEdge("generate", StateGraph.END);
                // 조건부 병렬 fan-out(소스별 라우팅은 CompletableFuture<String> 반환 필요)
//                graph.addConditionalEdges("source_select",
//                        edge_async(s -> {
//                            List<String> selected = s.<List<String>>value(DraftState.SOURCES)
//                                    .orElse(Collections.emptyList());
//                            return selected.contains("web") ? "web" : "skip_web"; // ← String 반환
//                        }),
//                        Map.of("web","web_branch","skip_web","aggregate"));
//                graph.addConditionalEdges("source_select",
//                        edge_async(s -> {
//                            List<String> selected = s.<List<String>>value(DraftState.SOURCES)
//                                    .orElse(Collections.emptyList());
//                            return selected.contains("news") ? "news" : "skip_news"; // ← String 반환
//                        }),
//                        Map.of("news","news_branch","skip_news","aggregate"));
//                graph.addConditionalEdges("source_select",
//                        edge_async(s -> {
//                            List<String> selected = s.<List<String>>value(DraftState.SOURCES)
//                                    .orElse(Collections.emptyList());
//                            return selected.contains("db") ? "db" : "skip_db"; // ← String 반환
//                        }),
//                        Map.of("db","db_branch","skip_db","aggregate"));
//                graph.addEdge("web_branch","aggregate");
//                graph.addEdge("news_branch","aggregate");
//                graph.addEdge("db_branch","aggregate");
//                graph.addEdge("aggregate","generate");
//                graph.addEdge("generate","validate");
//                graph.addConditionalEdges("validate",
//                        edge_async(s -> {
//                            boolean ok = s.<Boolean>value(DraftState.IS_VALID).orElse(false);
//                            return ok ? "END" : "retry_adjust";        // ← String 반환 (동기 EdgeAction)
//                        }),
//                        Map.of("END", StateGraph.END, "retry_adjust", "retry_adjust"));
//                graph.addEdge("retry_adjust","generate");

        return graph.compile(); // 실행용 그래프 생성
    }
}
package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.DbSubgraphInvoker;
import com.example.demo.langgraph.nodes.*;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.*;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final PromptSelectorNode promptSelector;
    private final SourceSelectorNode sourceSelector;
    //    private final WebSubgraphInvoker webSubgraphInvoker; // 웹검색 RAG 서브 랭그래프
//    private final NewsSubgraphInvoker newsSubgraphInvoker; // 뉴스검색 RAG 서브 랭그래프
    private final DbSubgraphInvoker dbSubgraphInvoker; // DB검색 RAG 서브 랭그래프
    private final ContextAggregatorNode contextAggregator; // RAG Context 병합 노드
    private final DraftGeneratorNode draftGenerator;
    private final ValidatorGraphInvokerNode validatorInvoker;


    @Bean
    public CompiledGraph<DraftState> draftGraph() throws GraphStateException {
        // StateGraph 인스턴스 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(DraftState.SCHEMA);
        StateGraph<DraftState> graph = new StateGraph<>(schema, DraftState::new);

        // 노드정의
        graph.addNode("prompt", promptSelector);
        graph.addNode("source_select", sourceSelector);
//                graph.addNode("web_branch",    node_async(webSubgraphInvoker));
//                graph.addNode("news_branch",   node_async(newsSubgraphInvoker));
        graph.addNode("db_branch",     dbSubgraphInvoker);
        graph.addNode("aggregate",     contextAggregator);
        graph.addNode("generate", draftGenerator);
        graph.addNode("validate", validatorInvoker);

        // 엣지연결
        graph.addEdge(StateGraph.START, "prompt");
        graph.addEdge("prompt", "source_select");

        // source_select -> fan-out (db)
        graph.addConditionalEdges("source_select",
                edge_async(s -> {
                    List<String> selected = s.<List<String>>value(DraftState.SOURCES)
                            .orElse(Collections.emptyList());
                    return selected.contains("db") ? "db" : "skip_db_db";
                }),
                Map.of("db", "db_branch", "skip_db_db", "aggregate")
        );

//        // source_select -> fan-out (web)
//        graph.addConditionalEdges("source_select",
//                edge_async(s -> {
//                    List<String> selected = s.<List<String>>value(DraftState.SOURCES)
//                            .orElse(Collections.emptyList());
//                    return selected.contains("web") ? "web" : "skip_web";
//                }),
//                Map.of("web", "web_branch", "skip_web", "aggregate")
//        );
//
//        // source_select -> fan-out (news)
//        graph.addConditionalEdges("source_select",
//                edge_async(s -> {
//                    List<String> selected = s.<List<String>>value(DraftState.SOURCES)
//                            .orElse(Collections.emptyList());
//                    return selected.contains("news") ? "news" : "skip_news";
//                }),
//                Map.of("news", "news_branch", "skip_news", "aggregate")
//        );


        graph.addEdge("db_branch", "aggregate");
//        graph.addEdge("news_branch", "aggregate");
//        graph.addEdge("web_branch", "aggregate");

        // aggregate → 조건부 엣지
        graph.addConditionalEdges("aggregate",
                edge_async(state -> {
                    boolean dbOk   = state.<Boolean>value(DraftState.DB_READY).orElse(false);
//                    boolean webOk  = state.<Boolean>value(DraftState.WEB_READY).orElse(false);
//                    boolean newsOk = state.<Boolean>value(DraftState.NEWS_READY).orElse(false);

                    if (!dbOk)   return "db";    // DB 실패 → 다시 DB 브랜치
//                    if (!webOk)  return "web";   // Web 실패 → 다시 Web 브랜치
//                    if (!newsOk) return "news";  // News 실패 → 다시 News 브랜치

                    return "generate"; // 모두 성공 → generate 노드로
                }),
                Map.of(
                        "db", "db_branch",
//                        "web", "web_branch",
//                        "news", "news_branch",
                        "generate", "generate"
                )
        );

        graph.addEdge("generate", "validate");
        graph.addEdge("validate", StateGraph.END);
//        graph.addEdge("generate", StateGraph.END);

        return graph.compile(); // 실행용 그래프 생성
    }
}
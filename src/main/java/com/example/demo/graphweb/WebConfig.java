// 여기에 node와 state가 어떻게 연결되는지 만들기.
package com.example.demo.graphweb;

import com.example.demo.graphweb.nodes.*;

import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final QueryBuilderNode queryBuilderNode;
    private final SearchNode searchNode;
    private final FetchNode fetchNode;
    private final ValidationNode validationNode;
    private final PickedArticleNode pickedArticleNode;
    private final AfterValidationNode afterValidationNode;
    private final ProcessNode processnode;

    @Bean(name = "webSubGraph")
    public CompiledGraph<WebState> webSubGraph() throws GraphStateException {

        // ✅ WebState 기반으로 StateGraph 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(WebState.SCHEMA);
        StateGraph<WebState> graph = new StateGraph<>(schema, WebState::new);

        // ✅ 노드 정의
        graph.addNode("query", queryBuilderNode);
        graph.addNode("search", searchNode);
        graph.addNode("picked", pickedArticleNode);
        graph.addNode("fetch", fetchNode);
        graph.addNode("process", processnode);
        graph.addNode("validation", validationNode);
        graph.addNode("afterValidation", afterValidationNode);

        // 엣지 설정
        graph.addEdge(START, "query");
        graph.addEdge("query", "search");
        graph.addEdge("search", "picked");
        graph.addEdge("picked", "fetch");
        graph.addEdge("fetch", "process");
        graph.addEdge("process", "validation");
        graph.addEdge("validation","afterValidation");
        graph.addConditionalEdges("afterValidation",
                edge_async(s -> "end".equals(s.value(WebState.DECISION).orElse("")) ? END : "picked"),
                Map.of(END, END, "picked", "picked")
        );

        return graph.compile();
    }
}

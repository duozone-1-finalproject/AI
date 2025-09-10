// 여기에 node와 state가 어떻게 연결되는지 만들기.
package com.example.demo.graphweb;

import com.example.demo.graphweb.nodes.QueryBuilderNode;
import com.example.demo.graphweb.nodes.SearchNode;
import com.example.demo.graphweb.nodes.FetchNode;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final QueryBuilderNode queryBuilderNode;
    private final SearchNode searchNode;
    private final FetchNode fetchNode;

    @Bean(name = "webSubGraph")
    public CompiledGraph<WebState> webSubGraph() throws GraphStateException {

        // ✅ WebState 기반으로 StateGraph 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(WebState.SCHEMA);
        StateGraph<WebState> graph = new StateGraph<>(schema, WebState::new);

        // ✅ 노드 정의
        graph.addNode("query", queryBuilderNode);
        graph.addNode("search", searchNode);
        graph.addNode("fetch", fetchNode);

        // ✅ 엣지 연결 (실행 순서: query → search → fetch)
        graph.addEdge(StateGraph.START, "query");
        graph.addEdge("query", "search");
        graph.addEdge("search", "fetch");
        graph.addEdge("fetch", StateGraph.END);

        // ✅ 실행용 그래프 컴파일 후 반환
        return graph.compile();
    }
}






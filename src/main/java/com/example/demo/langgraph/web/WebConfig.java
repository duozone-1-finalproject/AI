// 여기에 node와 state가 어떻게 연결되는지 만들기.
package com.example.demo.langgraph.web;

import com.example.demo.langgraph.web.nodes.*;
import com.example.demo.langgraph.web.service.DuckService;
import com.example.demo.langgraph.web.service.WebService;
import com.example.demo.langgraph.web.state.WebState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.Graph;
import org.bsc.langgraph4j.builder.GraphBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public DuckService duckService() {
        return new DuckService();
    }

    @Bean
    public WebService webService(DuckService duckService) {
        return new WebService(duckService);
    }

    @Bean
    public QueryBuilderNode queryBuilderNode() {
        return new QueryBuilderNode();
    }

    @Bean
    public SearchNode searchNode(WebService webService) {
        return new SearchNode(webService);
    }

    @Bean
    public SummaryNode summaryNode() {
        return new SummaryNode();
    }
    // ✅ Node와 State를 연결하는 Graph 정의 (검색까지만)
    @Bean
    public CompiledGraph<WebState> webGraph(QueryBuilderNode queryBuilderNode,
                                            SearchNode searchNode,
                                            SummaryNode summaryNode) {
        GraphBuilder<WebState> builder = GraphBuilder.create(WebState.SCHEMA);

        builder.addNode("queryBuilder", queryBuilderNode);
        builder.addNode("search", searchNode);
        builder.addNode("summary", summaryNode);

// 실행 순서 정의: 쿼리 작성 → 검색 → 요약
        builder.addEdge("queryBuilder", "search");
        builder.addEdge("search", "summary");

        Graph<WebState> graph = builder.build();
        return graph.compile();
    }
}






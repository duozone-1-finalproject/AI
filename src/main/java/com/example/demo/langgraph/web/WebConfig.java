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

    /*@Bean
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
    */

    @Bean(name = "webSubGraph")
    public CompiledGraph<WebState> webSubGraph(QueryBuilderNode queryBuilderNode,
                                               SearchNode searchNode,
                                               SummaryNode summaryNode) {
// WebState 기반으로 StateGraph 생성
        StateGraph.Builder<WebState> builder = StateGraph.builder(WebState.SCHEMA);


// 노드 등록
        builder.addNode("query", queryBuilderNode);
        builder.addNode("search", searchNode);
        builder.addNode("summary", summaryNode);


// 실행 순서: query → search → summary
        builder.addEdge("query", "search");
        builder.addEdge("search", "summary");


// 시작/종료 지점
        builder.setEntryPoint("query");
        builder.setExitPoint("summary");


// 컴파일 후 반환
        return builder.build().compile();
    }
}






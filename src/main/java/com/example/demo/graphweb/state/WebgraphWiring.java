package com.example.demo.graphweb.state;

import com.example.demo.graphmain.web.nodes.*;
import com.example.demo.graphweb.nodes.QueryBuilderNode;
import com.example.demo.graphweb.nodes.SearchNode;
import com.example.demo.graphweb.nodes.SummaryNode;
import org.bsc.langgraph4j.graph.Graph;
import org.bsc.langgraph4j.graph.GraphBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebgraphWiring {

    @Bean(name = "webSubGraph")
    public Graph webSubGraph(QueryBuilderNode queryBuilderNode,
                             SearchNode searchNode,
                             ValidationNode validationNode,
                             SummaryNode summaryNode) {

        return GraphBuilder.builder()
                .addNode("query", queryBuilderNode)
                .addNode("search", searchNode)
                .addNode("validate", validationNode)
                .addNode("summary", summaryNode)
                .addEdge("query", "search")
                .addEdge("search", "validate")
                .addEdge("validate", "summary")
                .setEntryPoint("query")
                .setExitPoint("summary")
                .build();
    }
}


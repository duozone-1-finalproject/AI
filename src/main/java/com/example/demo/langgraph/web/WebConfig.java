package com.example.demo.langgraph.web;

import com.example.demo.langgraph.web.nodes.*;
import com.example.demo.langgraph.web.service.DuckService;
import com.example.demo.langgraph.web.service.WebService;
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
    public ValidationNode validationNode() {
        return new ValidationNode();
    }

    @Bean
    public SummaryNode summaryNode() {
        return new SummaryNode();
    }
}






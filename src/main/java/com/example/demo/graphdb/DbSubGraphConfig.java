package com.example.demo.graphdb;

import com.example.demo.graphdb.nodes.*;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Configuration
@RequiredArgsConstructor
public class DbSubGraphConfig {
    private final FilterCriteriaNode filterCriteriaNode;
    private final FinancialsProcessorNode financialsProcessorNode;
    private final CorpCodeRetrievalNode corpCodeRetrievalNode;
    private final ReportContentRetrievalNode reportContentRetrievalNode;
    private final DataPreprocessorNode dataPreprocessorNode;


    @Bean
    public CompiledGraph<DbSubGraphState> dbSubGraph() throws GraphStateException {
        // DbSubGraphState를 사용한 StateGraph 생성
        StateGraph<DbSubGraphState> dbSubGraph = new StateGraph<>(DbSubGraphState.SCHEMA, DbSubGraphState::new);

        // 노드 추가
        dbSubGraph.addNode("filter_criteria", filterCriteriaNode);
        dbSubGraph.addNode("financials_processor", financialsProcessorNode);
        dbSubGraph.addNode("code_retrieval", corpCodeRetrievalNode);
        dbSubGraph.addNode("report_retrieval", reportContentRetrievalNode);
        dbSubGraph.addNode("data_preprocessor", dataPreprocessorNode);

        // 엣지 추가
        dbSubGraph.addEdge(StateGraph.START, "filter_criteria");
        dbSubGraph.addConditionalEdges("filter_criteria",
                edge_async(s -> {
                    String filtered = s.<String>value(DbSubGraphState.FILTER_CRITERIA).orElse("");
                    return "financial".equals(filtered) ? "fin_process" : "skip_fin_process";
                }),
                Map.of(
                        "fin_process", "financials_processor",
                        "skip_fin_process", "code_retrieval"
                )
        );
        dbSubGraph.addEdge("financials_processor", "code_retrieval");
        dbSubGraph.addEdge("code_retrieval", "report_retrieval");
        dbSubGraph.addEdge("report_retrieval", "data_preprocessor");
        dbSubGraph.addEdge("data_preprocessor", StateGraph.END);

        return dbSubGraph.compile();
    }
}

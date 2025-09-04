package com.example.demo.dbsubgraph;

import com.example.demo.dbsubgraph.nodes.CorpCodeRetrievalNode;
import com.example.demo.dbsubgraph.nodes.DataPreprocessorNode;
import com.example.demo.dbsubgraph.nodes.FilterCriteriaNode;
import com.example.demo.dbsubgraph.nodes.ReportContentRetrievalNode;
import com.example.demo.dbsubgraph.state.DbSubGraphState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DbSubGraphConfig {
    private final FilterCriteriaNode filterCriteriaNode;
    private final CorpCodeRetrievalNode corpCodeRetrievalNode;
    private final ReportContentRetrievalNode reportContentRetrievalNode;
    private final DataPreprocessorNode dataPreprocessorNode;


    @Bean
    public CompiledGraph<DbSubGraphState> dbSubGraph() throws GraphStateException {
        // DbSubGraphState를 사용한 StateGraph 생성
        StateGraph<DbSubGraphState> dbSubGraph = new StateGraph<>(DbSubGraphState.SCHEMA, DbSubGraphState::new);

        // 노드 추가
        dbSubGraph.addNode("filter_criteria", filterCriteriaNode);
        dbSubGraph.addNode("code_retrieval", corpCodeRetrievalNode);
        dbSubGraph.addNode("report_retrieval", reportContentRetrievalNode);
        dbSubGraph.addNode("data_preprocessor", dataPreprocessorNode);

        // 엣지 추가
        dbSubGraph.addEdge(StateGraph.START, "filter_criteria");
        dbSubGraph.addEdge("filter_criteria", "code_retrieval");
        dbSubGraph.addEdge("code_retrieval", "report_retrieval");
        dbSubGraph.addEdge("report_retrieval", "data_preprocessor");
        dbSubGraph.addEdge("data_preprocessor", StateGraph.END);

        // 컴파일된 그래프 반환
        return dbSubGraph.compile();
    }
}

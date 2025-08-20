package com.example.demo.langgraph;

import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@RequiredArgsConstructor
public class GraphConfig {
    private final StateGraph<AgentState> graph;
    private final LlmNode llmNode;
    /**
     * 가장 작은 LangGraph 예제
     * State keys: input(String), output(String)
     * Flow: START -> llm -> END
     */
    @Bean
    public CompiledGraph<AgentState> demoGraph() throws GraphStateException {
        // 1) 노드 등록
        graph.addNode("llm", llmNode);
        // 2) 엣지 연결
        graph.addEdge(StateGraph.START, "llm");
        graph.addEdge("llm", StateGraph.END);
        // 3) 컴파일
        return graph.compile();
    }
}

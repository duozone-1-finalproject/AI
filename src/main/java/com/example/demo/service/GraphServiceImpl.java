package com.example.demo.service;

import com.example.demo.dto.LangGraphDto;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GraphServiceImpl implements GraphService {

    private final CompiledGraph<AgentState> graph;

    public GraphServiceImpl(CompiledGraph<AgentState> graph) {
        this.graph = graph;
    }

    @Override
    public LangGraphDto.GraphResult run(LangGraphDto.PromptRequest promptRequest) {
        var resultState = graph.invoke(Map.of("input", promptRequest)).orElse(new AgentState(Map.of()));
        return resultState.value("output")
                .map(LangGraphDto.GraphResult.class::cast)
                .orElse(new LangGraphDto.GraphResult(""));
    }
}

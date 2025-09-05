//service는 congig가 없어도 작동 가능하지만, config는 service없이 실행이 안됨
// 이 파일은

package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.*;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.*;


@Configuration
@RequiredArgsConstructor
public class DraftGraphConfig {

    private final PromptSelectorNode promptSelector;
    private final SourceSelectorNode sourceSelector;
    //    private final WebSubgraphInvoker webSubgraphInvoker; // 웹검색 RAG 서브 랭그래프
//    private final NewsSubgraphInvoker newsSubgraphInvoker; // 뉴스검색 RAG 서브 랭그래프
//    private final DbSubgraphInvoker dbSubgraphInvoker; // DB검색 RAG 서브 랭그래프
//    private final ContextAggregatorNode contextAggregator; // RAG Context 병합 노드
    private final DraftGeneratorNode draftGenerator;
    private final ValidatorGraphInvokerNode validatorInvoker;


    @Bean
    public CompiledGraph<DraftState> draftGraph() throws GraphStateException {
        // StateGraph 인스턴스 생성
        Map<String, Channel<?>> schema = new LinkedHashMap<>(DraftState.SCHEMA);
        StateGraph<DraftState> graph = new StateGraph<>(schema, DraftState::new);

        // 노드정의
        graph.addNode("prompt", promptSelector);
        graph.addNode("source_select", sourceSelector);
//                graph.addNode("web_branch",    node_async(webSubgraphInvoker));
//                graph.addNode("news_branch",   node_async(newsSubgraphInvoker));
//                graph.addNode("db_branch",     node_async(dbSubgraphInvoker));
//                graph.addNode("aggregate",     contextAggregator);
        graph.addNode("generate", draftGenerator);
        graph.addNode("validate", validatorInvoker);
        // 엣지연결
        graph.addEdge(StateGraph.START, "prompt");
        graph.addEdge("prompt", "source_select");
        graph.addEdge("source_select", "generate");
        graph.addEdge("generate", "validate");
        graph.addEdge("validate", StateGraph.END);

        return graph.compile(); // 실행용 그래프 생성
    }
}
package com.example.demo.langgraph;

import com.example.demo.langgraph.nodes.BuildPromptNode;
import com.example.demo.langgraph.nodes.GenerateDraftNode;
import com.example.demo.langgraph.nodes.RagSearchNode;
import com.example.demo.langgraph.nodes.ValidateAgainstStandardNode;
import com.example.demo.langgraph.state.RiskState;
import com.example.demo.service.ElasticsearchService;
import com.example.demo.service.SerpApiService;
import com.example.demo.service.PromptTemplateService;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.ai.chat.client.ChatClient;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public final class RiskGraphFactory {

    private RiskGraphFactory() {}

    // 1) 산업(Industry) 리스크 그래프
    public static StateGraph<RiskState> buildIndustryGraph(
            SerpApiService serp,
            ElasticsearchService es,
            PromptTemplateService prompts,
            ChatClient chat
    ) throws GraphStateException {

        return new StateGraph<>(RiskState.SCHEMA, RiskState::new)
                // NodeAction -> AsyncNodeAction 로 래핑
                .addNode("build_prompt", node_async(new BuildPromptNode(prompts, "industry_risk")))
                .addNode("rag",          node_async(new RagSearchNode(serp, es, "industry")))
                // 이미 AsyncNodeAction이면 래핑 불필요
                .addNode("gen",          new GenerateDraftNode(chat))
                .addNode("validate",     node_async(new ValidateAgainstStandardNode(es, "핵심투자위험")))
                // edges
                .addEdge(START,          "build_prompt")
                .addEdge("build_prompt", "rag")
                .addEdge("rag",          "gen")
                .addEdge("gen",          "validate")
                .addEdge("validate",     END);
    }

    // 2) 회사(Company) 리스크 그래프
    public static StateGraph<RiskState> buildCompanyGraph(
            SerpApiService serp,
            ElasticsearchService es,
            PromptTemplateService prompts,
            ChatClient chat
    ) throws GraphStateException {

        return new StateGraph<>(RiskState.SCHEMA, RiskState::new)
                .addNode("build_prompt", node_async(new BuildPromptNode(prompts, "company_risk")))
                .addNode("rag",          node_async(new RagSearchNode(serp, es, "company")))
                .addNode("gen",          new GenerateDraftNode(chat))
                .addNode("validate",     node_async(new ValidateAgainstStandardNode(es, "핵심투자위험")))
                .addEdge(START,          "build_prompt")
                .addEdge("build_prompt", "rag")
                .addEdge("rag",          "gen")
                .addEdge("gen",          "validate")
                .addEdge("validate",     END);
    }

    // 3) 기타(Etc) 리스크 그래프
    public static StateGraph<RiskState> buildEtcGraph(
            SerpApiService serp,
            ElasticsearchService es,
            PromptTemplateService prompts,
            ChatClient chat
    ) throws GraphStateException {

        return new StateGraph<>(RiskState.SCHEMA, RiskState::new)
                .addNode("build_prompt", node_async(new BuildPromptNode(prompts, "etc_risk")))
                .addNode("rag",          node_async(new RagSearchNode(serp, es, "etc")))
                .addNode("gen",          new GenerateDraftNode(chat))
                .addNode("validate",     node_async(new ValidateAgainstStandardNode(es, "핵심투자위험")))
                .addEdge(START,          "build_prompt")
                .addEdge("build_prompt", "rag")
                .addEdge("rag",          "gen")
                .addEdge("gen",          "validate")
                .addEdge("validate",     END);
    }
}

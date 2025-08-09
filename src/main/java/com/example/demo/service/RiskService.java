package com.example.demo.service;

import com.example.demo.dto.RiskRequest;
import com.example.demo.langgraph.RiskGraphFactory;
import com.example.demo.langgraph.state.RiskState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class RiskService {
    private final SerpApiService serp;
    private final ElasticsearchService es;
    private final PromptTemplateService prompts;
    private final ChatClient chat;

    public List<Map<String,String>> generate(RiskRequest req) throws Exception {
        // 1) 그래프 구성 & 컴파일
        StateGraph<RiskState> gIndustry = RiskGraphFactory.buildIndustryGraph(serp, es, prompts, chat);
        StateGraph<RiskState> gCompany  = RiskGraphFactory.buildCompanyGraph(serp, es, prompts, chat);
        StateGraph<RiskState> gEtc      = RiskGraphFactory.buildEtcGraph(serp, es, prompts, chat);

        CompiledGraph<RiskState> c1 = gIndustry.compile();  // StateGraph -> CompiledGraph
        CompiledGraph<RiskState> c2 = gCompany.compile();
        CompiledGraph<RiskState> c3 = gEtc.compile();

        // 2) 초기 입력 상태
        Map<String,Object> init = Map.of(
                "input", Map.of(
                        "corpName", req.corpName(),
                        "industry", req.industryKeyword()
                )
        );

        // 3) 병렬 실행 (invoke는 Optional<State>를 돌려줌)
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture<RiskState> f1 = CompletableFuture.supplyAsync(
                    () -> c1.invoke(init).orElseThrow(() -> new IllegalStateException("industry graph returned empty state")), pool);

            CompletableFuture<RiskState> f2 = CompletableFuture.supplyAsync(
                    () -> c2.invoke(init).orElseThrow(() -> new IllegalStateException("company graph returned empty state")), pool);

            CompletableFuture<RiskState> f3 = CompletableFuture.supplyAsync(
                    () -> c3.invoke(init).orElseThrow(() -> new IllegalStateException("etc graph returned empty state")), pool);

            RiskState s1 = f1.get(120, TimeUnit.SECONDS);
            RiskState s2 = f2.get(120, TimeUnit.SECONDS);
            RiskState s3 = f3.get(120, TimeUnit.SECONDS);

            return List.of(
                    Map.of("industry_risk", s1.finalText()),
                    Map.of("company_risk",  s2.finalText()),
                    Map.of("etc_risk",      s3.finalText())
            );
        } finally {
            pool.shutdown();
        }
    }
}
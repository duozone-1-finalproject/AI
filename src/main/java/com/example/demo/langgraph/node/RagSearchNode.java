package com.example.demo.langgraph.node;

import com.example.demo.service.ElasticsearchService;
import com.example.demo.service.SerpApiService;
import com.example.demo.langgraph.state.RiskState;
import reactor.core.publisher.Mono;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RagSearchNode implements NodeAction<RiskState> {
    private final SerpApiService serp;
    private final ElasticsearchService es;
    private final String ragType;

    public RagSearchNode(SerpApiService serp, ElasticsearchService es, String ragType) {
        this.serp = serp;
        this.es = es;
        this.ragType = ragType;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(RiskState state) {
        String corp = String.valueOf(state.input().getOrDefault("corpName",""));
        String industry = String.valueOf(state.input().getOrDefault("industry",""));

        Mono<List<SerpNewsItem>> newsMono;
        Mono<List<SerpNewsItem>> webMono;

        if ("industry".equals(ragType)) {
            newsMono = serp.searchNews(industry + " 위험 리스크 규제 변동 리콜 소송");
            webMono  = Mono.just(List.of());
        } else if ("company".equals(ragType)) {
            newsMono = serp.searchNews(corp + " 리콜 소송 규제 이슈 감사의견");
            webMono  = serp.searchWeb(corp + " 재무 위험 내부통제 이슈 site:dart.fss.or.kr");
        } else {
            newsMono = Mono.just(List.of());
            webMono  = serp.searchWeb((corp + " " + industry + " 공급망 차질 원자재 가격 환율 금리").trim());
        }

        Mono<List<Map<String,Object>>> esMono =
                Mono.fromCallable(() -> es.searchReportsSection(
                                new String[]{"rpt_biz","rpt_qt","rpt_half","rpt_sec_eq"},
                                corp, industry, 5))
                        .onErrorReturn(List.of());

        return Mono.zip(
                        newsMono.defaultIfEmpty(List.of()),
                        webMono.defaultIfEmpty(List.of()),
                        esMono.defaultIfEmpty(List.of())
                )
                .map(tuple -> {
                    var evid = new ArrayList<Object>();
                    tuple.getT1().forEach(n -> evid.add(Map.of(
                            "type","news", "title", n.title(), "url", n.link(), "summary", n.snippet()
                    )));
                    tuple.getT2().forEach(n -> evid.add(Map.of(
                            "type","web", "title", n.title(), "url", n.link(), "summary", n.snippet()
                    )));
                    tuple.getT3().forEach(m -> evid.add(Map.of(
                            "type","db",
                            "doc_name", String.valueOf(((Map<?,?>)m).getOrDefault("doc_name","")),
                            "corp_name", String.valueOf(((Map<?,?>)m).getOrDefault("corp_name","")),
                            // ES에서 inner_hits로 섹션 스니펫을 넣었다면 여기에 매핑
                            "content", ((Map<?,?>)m).getOrDefault("_inner_hits", ((Map<?,?>)m).getOrDefault("sections",""))
                    )));
                    return Map.<String,Object>of(RiskState.EVID, evid);
                })
                .toFuture(); // ✅ AsyncNodeAction이 요구하는 CompletableFuture로 변환
    }
}
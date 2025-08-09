package com.example.demo.langgraph.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiskState extends AgentState {
    public static final String INPUT   = "input";    // 요청 파라미터 맵
    public static final String EVID    = "evidence"; // 수집된 근거(뉴스/DB)
    public static final String DRAFT   = "draft";    // 모델 초안
    public static final String FINAL   = "final";    // 검증 후 최종
    public static final String CITES   = "citations";// 출처 목록

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            INPUT,  Channels.base(HashMap::new),       // 가변 맵
            EVID,   Channels.appender(ArrayList::new), // 리스트 누적
            DRAFT,  Channels.base(() -> ""),           // 마지막 값 보관
            FINAL,  Channels.base(() -> ""),
            CITES,  Channels.appender(ArrayList::new)  // 리스트 누적
    );

    public RiskState(Map<String, Object> init) {
        super(init);
    }

    // --- 헬퍼 ---
    @SuppressWarnings("unchecked")
    public Map<String, Object> input() {
        return this.<Map<String, Object>>value(INPUT).orElseGet(HashMap::new);
    }

    @SuppressWarnings("unchecked")
    public List<Object> evidence() {
        return this.<List<Object>>value(EVID).orElseGet(List::of); // 읽기 전용 뷰 OK
    }

    public String draft() {
        return this.<String>value(DRAFT).orElse("");
    }

    public String finalText() {
        return this.<String>value(FINAL).orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<Object> citations() {
        return this.<List<Object>>value(CITES).orElseGet(List::of);
    }


}

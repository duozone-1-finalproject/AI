// 저장소

package com.example.demo.langgraph.web.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

public class WebState extends AgentState {

    // ---- 키 상수 ----
    public static final String CORP_CODE = "corpCode";
    public static final String CORP_NAME = "corpName";
    public static final String IND_NAME = "indutyName";
    public static final String IND_CODE = "indutyCode";
    public static final String SECTION = "section";


    // 결과 키
    public static final String SUMMARIES = "summaries";
    public static final String QUERY = "query"; // QueryBuilderNode 결과
    public static final String ARTICLES = "articles"; // SearchNode 결과
    public static final String VALIDATED = "validated"; // ValidationNode 결과


    // 섹션 구분 상수 => main에서 정의한 section label
    /*
    public static final String SECTION_RISK_INDUSTRY = "산업위험";
    public static final String SECTION_RISK_COMPANY = "회사위험";
    */

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력값
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(SECTION, Channels.base(() -> "")),

            // 결과값
            Map.entry(QUERY, Channels.base(ArrayList<String>::new)),
            Map.entry(ARTICLES, Channels.appender(ArrayList::new)),
            Map.entry(SUMMARIES, Channels.appender(ArrayList::new))
            //Map.entry(VALIDATED, Channels.base(() -> Boolean.FALSE)) -> 검증노드 미구현. 추후 수정 예정
    );

    /* 생성자
    그래프 실행 시작 시 Map<String,Object> 초기 데이터를 받아서 AgentState(부모 클래스)에 전달.
    즉, state 초기화 담당 */

    public WebState(Map<String, Object> init) {
        super(init);
    }

    /* Getter 편의 메서드
    state 내부 값을 안전하게 꺼내는 도우미
    Node/Service에서 값을 읽을 때
    매번 state.value("키")라고 안 쓰고, 깔끔하게 state.getArticles()처럼 쓸 수 있게 해줌 */
    public List<String> getQueries() {
        return this.<List<String>>value(QUERY).orElse(List.of());
    }

    public List<?> getArticles() {
        return this.<List<?>>value(ARTICLES).orElse(List.of());
    }

    public List<?> getSummaries() {
        return this.<List<?>>value(SUMMARIES).orElse(List.of());
    }

    public boolean isValidated() {
        return this.<Boolean>value(VALIDATED).orElse(false);
    }}

// memo
/*
Channels.base와 Channels.appender의 차이
- Channels.base → 값을 덮어쓰는 채널 (corpName, section 같은 단일 값에 적합)
- Channels.appender → 값을 리스트에 누적하는 채널 (검색 결과 articles, summaries 같은 다중 값에 적합)
=> 즉, 단일 값이면 base, 여러 개 모아야 하면 appender
*/
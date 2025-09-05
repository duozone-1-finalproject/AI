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
    public static final String SUMMARIES = "summaries";

    // 섹션 구분 상수 (하드코딩 대신 사용)
    public static final String SECTION_RISK_INDUSTRY = "산업위험";
    public static final String SECTION_RISK_COMPANY = "회사위험";

    // 새로 추가
    public static final String QUERY = "query"; // QueryBuilderNode 결과
    public static final String ARTICLES = "articles"; // SearchNode 결과
    public static final String VALIDATED = "validated"; // ValidationNode 결과

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력값 (덮어쓰기)
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(SECTION, Channels.base(() -> "")),

            // 병렬 수집용 리스트 채널 (appender)
            Map.entry(SUMMARIES, Channels.appender(ArrayList::new)),


            // 쿼리 및 처리 결과
            Map.entry(QUERY, Channels.base(ArrayList<String>::new)),
            Map.entry(ARTICLES, Channels.base(ArrayList::new)),
            Map.entry(SUMMARIES, Channels.base(ArrayList::new)),
            Map.entry(VALIDATED, Channels.base(() -> Boolean.FALSE))
    );

    //생성자
    //그래프 실행 시작 시 Map<String,Object> 초기 데이터를 받아서 AgentState(부모 클래스)에 전달.
    //즉, state 초기화 담당.

    public WebState(Map<String, Object> init) {
        super(init);
    }

    // Getter 편의 메서드
    //state 내부 값을 안전하게 꺼내는 도우미
    //Node/Service에서 값을 읽을 때
    // 매번 state.value("키")라고 안 쓰고, 깔끔하게 state.getArticles()처럼 쓸 수 있게 해줌

    public List<?> getArticles() {
        return this.<List<?>>value(ARTICLES).orElse(List.of());
    }

    public List<?> getSummaries() {
        return this.<List<?>>value(SUMMARIES).orElse(List.of());
    }

    public boolean isValidated() {
        return this.<Boolean>value(VALIDATED).orElse(false);

    }

    @Override
    public void update(Map<String, Object> partial, Map<String, Channel<?>> schema) {
        super.update(partial, schema);
    }

    @Override
    public Map<String, Object> asMap() {
        return super.asMap();
    }

    public void set(String validated, boolean validated1) {
    }
}

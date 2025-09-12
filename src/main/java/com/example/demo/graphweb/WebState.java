// 저장소

package com.example.demo.graphweb;

import com.example.demo.dto.WebResponseDto;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebState extends AgentState {

    /*
    회사 위험, 산업 위험을 담아둔 키가
      SECTION_LABEL(한글), SECTION(영어)
     */

    // ---- 입력 키 ----
    public static final String CORP_NAME     = "corpName";
    public static final String IND_NAME      = "indutyName";   // (팀에서 industryName으로 바꿀 계획이면 alias 추가 고려)
    public static final String SECTION_LABEL = "sectionLabel";

    // ---- 내부 DTO (검색 단계용) ----
    public record Brief(String title, String url, String source, String date, String contents) {}
    public record KeywordBundle(String keyword, List<Brief> searched_data) {}

    // ---- 결과 키 ----
    public static final String QUERY        = "query";         // List<String> (QueryBuilderNode)
    public static final String ARTICLES     = "articles";      // List<KeywordBundle> (SearchNode 결과)
    public static final String FETCHED_ARTICLES = "fetched_articles"; // List<WebResponseDto.Article> (FetchNode 결과)
    public static final String VALIDATED    = "validated";     // Boolean (ValidationNode 결과)
    public static final String ERRORS       = "errors";        // List<String> (누적 에러 로그)
    public static final String FINAL_RESULT = "final_result";  // List<WebResponseDto.Article> (검증 통과 누적)

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력
            Map.entry(CORP_NAME,     Channels.base(() -> "")),
            Map.entry(IND_NAME,      Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),

            // 결과값
            Map.entry(QUERY, Channels.base(ArrayList<String>::new)),
            Map.entry(ARTICLES,   Channels.base(ArrayList<KeywordBundle>::new)),
            // FetchNode가 반환하는 List<Article>을 저장하기 위해 appender 채널을 사용합니다.
            Map.entry(FETCHED_ARTICLES, Channels.<WebResponseDto.Article>appender(ArrayList::new)),
            Map.entry(VALIDATED, Channels.base(() -> Boolean.FALSE)),
            // 검증노드에서 통과를 못한 경우 (END로 안가는 경우, 루프되는 노드로 보낼때의 데이터를 어떤걸 보낼건가?
            //Map.entry(FINAL_RESULT, Channels.appender(ArrayList::new)),  // 통과했을 경우 넣을 state
            Map.entry(FINAL_RESULT, Channels.<WebResponseDto.Article>appender(ArrayList::new)),
            // [{keyword:"산업전망", searched_data: [{title, url, source, date}]}, ]
            Map.entry(ERRORS, Channels.appender(ArrayList::new))

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

    // ---- Getters ----
    public String getCorpName()     { return this.<String>value(CORP_NAME).orElse(""); }
    public String getIndName()      { return this.<String>value(IND_NAME).orElse(""); }
    public String getSectionLabel() { return this.<String>value(SECTION_LABEL).orElse(""); }

    public List<String> getQueries() {
        return this.<List<String>>value(QUERY).orElse(List.of());
    }

    // 🔸 SearchNode가 저장한 "키워드별 Top3" 번들
    public List<KeywordBundle> getArticles() {
        return this.<List<KeywordBundle>>value(ARTICLES).orElse(List.of());
    }

    // 🔸 FetchNode가 저장한 "본문이 채워진" 기사 목록
    public List<WebResponseDto.Article> getFetchedArticles() {
        return this.<List<WebResponseDto.Article>>value(FETCHED_ARTICLES).orElse(List.of());
    }

    public boolean isValidated() {
        return this.<Boolean>value(VALIDATED).orElse(false);
    }

    public List<WebResponseDto.Article> getFinalResult() {
        return this.<List<WebResponseDto.Article>>value(FINAL_RESULT).orElse(List.of());
    }

    public List<String> getErrors() {
        return this.<List<String>>value(ERRORS).orElse(List.of());
    }
}

// memo
/*
Channels.base와 Channels.appender의 차이
- Channels.base → 값을 덮어쓰는 채널 (corpName, section 같은 단일 값에 적합)
- Channels.appender → 값을 리스트에 누적하는 채널 (검색 결과 articles, summaries 같은 다중 값에 적합)
=> 즉, 단일 값이면 base, 여러 개 모아야 하면 appender
*/
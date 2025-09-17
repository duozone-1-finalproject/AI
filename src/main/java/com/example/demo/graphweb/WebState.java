// 저장소

package com.example.demo.graphweb;

import com.example.demo.dto.graphweb.SearchLLMDto;
import com.example.demo.dto.graphweb.WebDocs;
import com.example.demo.dto. graphweb.WebResponseDto;
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

    // ---- 결과 키 ----
    public static final String QUERY        = "query";         // List<String> (QueryBuilderNode)
    public static final String ARTICLES     = "articles";      // 💡 [수정] List<SearchLLMDto> (SearchNode 결과)
    public static final String FETCHED_ARTICLES = "fetched_articles"; // List<WebResponseDto.Article> (FetchNode 결과)
    public static final String VALIDATED    = "validated";     // Boolean (ValidationNode 결과)
    public static final String ERRORS       = "errors";        // List<String> (누적 에러 로그)
    public static final String FINAL_RESULT = "final_result";  // List<WebResponseDto.Article> (검증 통과 누적)
    public static final String CUR_KEY_IDX = "cur_key_idx";
    public static final String CUR_CAND_IDX = "cur_cand_idx";
    public static final String PICKED_ARTICLE = "picked_article";
    public static final String CUR_KEYWORD = "cur_keyword";
    public static final String WEB_DOCS = "web_docs";
    public static final String DECISION = "decsion";
    public static final String PRO_ARTICLES = "pro_articles";

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력
            Map.entry(CORP_NAME,     Channels.base(() -> "")),
            Map.entry(IND_NAME,      Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),

            // 결과값
            Map.entry(QUERY, Channels.base(ArrayList<String>::new)),
            Map.entry(ARTICLES,   Channels.base(ArrayList<SearchLLMDto>::new)), // 💡 [수정] 저장할 타입을 SearchLLMDto의 리스트로 변경
            // FetchNode가 반환하는 List<Article>을 저장하기 위해 appender 채널을 사용합니다.
            Map.entry(FETCHED_ARTICLES, Channels.base(() -> "")),
            Map.entry(VALIDATED, Channels.base(() -> Boolean.FALSE)),
            // 검증노드에서 통과를 못한 경우 (END로 안가는 경우, 루프되는 노드로 보낼때의 데이터를 어떤걸 보낼건가?
            //Map.entry(FINAL_RESULT, Channels.appender(ArrayList::new)),  // 통과했을 경우 넣을 state
            Map.entry(FINAL_RESULT, Channels.<WebResponseDto.Article>appender(ArrayList::new)),
            // [{keyword:"산업전망", searched_data: [{title, url, source, date}]}, ]
            Map.entry(ERRORS, Channels.appender(ArrayList::new)),
            Map.entry(CUR_KEY_IDX, Channels.base(() -> 0)),
            Map.entry(CUR_CAND_IDX, Channels.base(() -> 0)),
            Map.entry(PICKED_ARTICLE, Channels.base(SearchLLMDto.Item::new)),
            Map.entry(CUR_KEYWORD, Channels.base(() -> "")),
            Map.entry(WEB_DOCS, Channels.appender(ArrayList<WebDocs>::new)),
            Map.entry(PRO_ARTICLES, Channels.base(() -> "")),
            Map.entry(DECISION, Channels.base(() -> ""))
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

    // 🔸 SearchNode가 저장한 "키워드별 검색 결과"
    public List<SearchLLMDto> getArticles() {
        return this.<List<SearchLLMDto>>value(ARTICLES).orElse(List.of());
    }

    // 🔸 FetchNode가 저장한 "본문이 채워진" 기사 목록
    public String getFetchedArticles() {
        return this.<String>value(FETCHED_ARTICLES).orElse("");
    }

    public boolean isValidated() {
        return this.<Boolean>value(VALIDATED).orElse(false);
    }

    public List<com.example.demo.dto.graphweb.WebResponseDto.Article> getFinalResult() {
        return this.<List<WebResponseDto.Article>>value(FINAL_RESULT).orElse(List.of());
    }

    public List<String> getErrors() {
        return this.<List<String>>value(ERRORS).orElse(List.of());
    }
    public SearchLLMDto.Item getPickedArticle() {
        return this.<SearchLLMDto.Item>value(PICKED_ARTICLE).orElseGet(SearchLLMDto.Item::new);
    }

    public Integer getCurKeyIdx() {
        return this.<Integer>value(CUR_KEY_IDX).orElse(0);
    }

    public Integer getCurCandIdx() {
        return this.<Integer>value(CUR_CAND_IDX).orElse(0);
    }

    public Boolean getValidated() {
        return this.<Boolean>value(VALIDATED).orElse(false);
    }

    public String getCurKeyword() {
        return this.<String>value(CUR_KEYWORD).orElse("");
    }

    // WebState.ARTICLES의 size 얻는 getter
    public Integer getArticlesSize() {
        return this.<List<SearchLLMDto>>value(ARTICLES).orElse(List.of()).size();
    }
    // WebState.ARTICLES의 CUR_KEY_IDX의 candidates의 size 얻는 getter
    public Integer getCandidatesSize(Integer keyIdx) {
        return this.<List<SearchLLMDto>>value(ARTICLES).orElse(List.of()).get(keyIdx).getCandidates().size();
    }
    public String getDecision() {
        return this.<String>value(DECISION).orElse("");
    }

    public List<WebDocs> getWebDocs() {
        return this.<List<WebDocs>>value(WEB_DOCS).orElse(List.of());
    }

    public String getProArticles() { return this.<String>value(PRO_ARTICLES).orElse("");

    }
}

// memo
/*
Channels.base와 Channels.appender의 차이
- Channels.base → 값을 덮어쓰는 채널 (corpName, section 같은 단일 값에 적합)
- Channels.appender → 값을 리스트에 누적하는 채널 (검색 결과 articles, summaries 같은 다중 값에 적합)
=> 즉, 단일 값이면 base, 여러 개 모아야 하면 appender
*/
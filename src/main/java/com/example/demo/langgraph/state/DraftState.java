//LangGraph에서 사용하는 상태(State) 클래스
//그래프 실행 중에 주고받는 데이터(회사명, 산업명, 기사 결과, 요약 등)를 담는 컨테이너** 역할

package com.example.demo.langgraph.state;

import com.example.demo.dto.ContextDoc;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

//LangGraph4j에서 제공하는 기본 State 관리 기능을 확장한 클래스

public class DraftState extends AgentState {

    // ---- 키 상수 ----
    public static final String CORP_CODE = "corpCode";
    public static final String CORP_NAME = "corpName";
    public static final String IND_CODE  = "indutyCode";
    public static final String IND_NAME  = "indutyName";
    public static final String RPT_EXIST = "rptExist";
    public static final String SECTION   = "sectionName";
    public static final String PROMPT    = "prompt";
    public static final String SOURCES   = "selectedSources"; // ex) ["web","news","db"]

    // 소스별 컨텍스트 (병렬 수집 → 반드시 appender)
    public static final String WEB_DOCS  = "webDocs";   // List<Doc>
    public static final String NEWS_DOCS = "newsDocs";  // List<Doc>
    public static final String DB_DOCS   = "dbDocs";    // List<Doc>
    public static final String CONTEXT   = "contextDocs"; // fan-in 결과 (List<Doc>)

    // 생성/검증/루프
    public static final String DRAFT     = "draftText";
    public static final String IS_VALID  = "isValid";
    public static final String VAL_SCORE = "validationScore";
    public static final String VAL_FB    = "validationFeedback"; // List<String>
    public static final String ATTEMPTS  = "attemptCount";
    public static final String ERRORS    = "errors"; // List<String>

    // ---- SCHEMA: base vs appender 구분 ----
    // 각 키와 채널(Channel)의 매핑 정의
    // 이때 `Channels.appender(...)`: 리스트처럼 누적(append)할 때 사용.

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력/메타 (덮어쓰기)
            Map.entry(CORP_CODE, Channels.base(() -> "")),
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE,  Channels.base(() -> "")),
            Map.entry(IND_NAME,  Channels.base(() -> "")),
            Map.entry(RPT_EXIST, Channels.base(() -> Boolean.FALSE)),
            Map.entry(SECTION,   Channels.base(() -> "")),
            Map.entry(PROMPT,    Channels.base(() -> "")),
            // 선택 소스는 1회만 쓰면 base, 동적 누적이면 appender로 바꿔도 됨
            Map.entry(SOURCES,   Channels.base(ArrayList<String>::new)),

            // 병렬 수집용 리스트 채널 (appender)
            Map.entry(WEB_DOCS,  Channels.appender(ArrayList::new)),
            Map.entry(NEWS_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(DB_DOCS,   Channels.appender(ArrayList::new)),
            Map.entry(CONTEXT,   Channels.base(() -> new ArrayList<ContextDoc>())),

            // 생성/검증/루프 (덮어쓰기 + 피드백은 누적)
            Map.entry(DRAFT,     Channels.base(() -> "")),
            Map.entry(IS_VALID,  Channels.base(() -> Boolean.FALSE)),
            Map.entry(VAL_SCORE, Channels.base(() -> 0.0)),
            Map.entry(VAL_FB,    Channels.appender(ArrayList::new)),
            Map.entry(ATTEMPTS,  Channels.base(() -> 0)),
            Map.entry(ERRORS,    Channels.appender(ArrayList::new))
    );

    //생성자: `init` 값을 초기 상태로 받아 부모 생성자에 전달.
    public DraftState(Map<String, Object> init) { super(init); }

    // (선택) 깔끔한 getter 몇 개만 노출
    public List<?> webDocs()  { return this.<List<?>>value(WEB_DOCS).orElse(List.of()); }
    public List<?> newsDocs() { return this.<List<?>>value(NEWS_DOCS).orElse(List.of()); }
    public List<?> dbDocs()   { return this.<List<?>>value(DB_DOCS).orElse(List.of()); }
}

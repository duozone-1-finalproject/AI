//LangGraph에서 사용하는 상태(State) 클래스
//그래프 실행 중에 주고받는 데이터(회사명, 산업명, 기사 결과, 요약 등)를 담는 컨테이너** 역할

package com.example.demo.langgraph.state;

import com.example.demo.dto.dbsubgraph.DbDocDto;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

//LangGraph4j에서 제공하는 기본 State 관리 기능을 확장한 클래스

public class DraftState extends AgentState {

    // ---- 키 상수 ----
    public static final String CORP_CODE = "corpCode";
    public static final String CORP_NAME = "corpName";
    public static final String IND_CODE = "indutyCode";
    public static final String IND_NAME = "indutyName";
    public static final String RPT_EXIST = "rptExist";

    public static final String SUMMARIES = "summaries";

    // ---- 섹션 타입 상수 ----
    // public static final String SECTION_RISK_INDUSTRY = "산업 위험";   // 산업위험
    // public static final String SECTION_RISK_COMPANY  = "사업 위험";   // 회사위험
    //public static final String SECTION_RISK_ETC      = "기타 위험";   // 기타위험 (확장 가능)

    public static final String SECTION = "section";
    public static final String SECTION_LABEL = "sectionLabel";
    public static final String PROMPT = "prompt";
    public static final String SOURCES = "sources"; // ex) ["web","news","db"]
    public static final String FINANCIALS = "financials";


    // 소스별 컨텍스트 (병렬 수집 → 반드시 appender)
    public static final String WEB_DOCS = "webDocs";   // List<Doc>
    public static final String NEWS_DOCS = "newsDocs";  // List<Doc>
    public static final String DB_DOCS = "dbDocs";    // List<Doc>

    // 소스별 실행 완료 여부
    public static final String DB_READY = "dbReady";
    public static final String WEB_READY = "webReady";
    public static final String NEWS_READY = "newsReady";

    // 생성/검증/루프
    public static final String DRAFT = "draft";
    public static final String ERRORS = "errors"; // List<String>

    // ---- SCHEMA: base vs appender 구분 ----
    // 각 키와 채널(Channel)의 매핑 정의
    // 이때 `Channels.appender(...)`: 리스트처럼 누적(append)할 때 사용.

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력/메타 (덮어쓰기)
            Map.entry(CORP_CODE, Channels.base(() -> "")),
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(RPT_EXIST, Channels.base(() -> false)),
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),
            Map.entry(PROMPT, Channels.base(() -> "")),
            // 선택 소스는 1회만 쓰면 base, 동적 누적이면 appender로 바꿔도 됨
            Map.entry(SOURCES, Channels.base(() -> new ArrayList<String>())),
            Map.entry(FINANCIALS, Channels.base(() -> "")),

            // 병렬 수집용 리스트 채널 (appender)
            Map.entry(WEB_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(NEWS_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(DB_DOCS, Channels.appender(ArrayList<DbDocDto>::new)),

            // 소스별 실행 완료 여부
            Map.entry(DB_READY, Channels.base(() -> false)),
            Map.entry(WEB_READY, Channels.base(() -> false)),
            Map.entry(NEWS_READY, Channels.base(() -> false)),
      
            Map.entry(SUMMARIES, Channels.appender(ArrayList::new)),


            // 생성/검증/루프 (덮어쓰기 + 피드백은 누적)
            Map.entry(DRAFT, Channels.appender(ArrayList::new)),
            Map.entry(ERRORS, Channels.appender(ArrayList::new))
    );


    public DraftState(Map<String, Object> init) {
        super(init);
    }

    // (선택) 깔끔한 getter 몇 개만 노출
    public List<String> webDocs() {
        return this.<List<String>>value(WEB_DOCS).orElse(List.of());
    }

    public List<String> newsDocs() {
        return this.<List<String>>value(NEWS_DOCS).orElse(List.of());
    }

    public List<DbDocDto> dbDocs() { return this.<List<DbDocDto>>value(DB_DOCS).orElse(List.of()); }
}

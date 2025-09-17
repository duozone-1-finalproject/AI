//LangGraph에서 사용하는 상태(State) 클래스
//그래프 실행 중에 주고받는 데이터(회사명, 산업명, 기사 결과, 요약 등)를 담는 컨테이너** 역할

package com.example.demo.graphmain;

import com.example.demo.dto.graphweb.WebDocs;
import com.example.demo.dto.graphdb.DbDocDto;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//LangGraph4j에서 제공하는 기본 State 관리 기능을 확장한 클래스

public class DraftState extends AgentState {

    // ---- 키 상수 ----
    public static final String CORP_CODE = "corpCode";
    public static final String CORP_NAME = "corpName";
    public static final String IND_CODE = "indutyCode";
    public static final String IND_NAME = "indutyName";
    public static final String SECTION = "section";
    public static final String SECTION_LABEL = "sectionLabel";
    public static final String SOURCES = "sources"; // ex) ["web","news","db"]
    public static final String FINANCIALS = "financials";

    // 소스별 컨텍스트 (병렬 수집 → 반드시 appender)
    public static final String WEB_DOCS = "webDocs";   // List<Doc>// List<Doc>
    public static final String DB_DOCS = "dbDocs";    // List<Doc>

    // 소스별 실행 완료 여부
    public static final String DB_READY = "dbReady";
    public static final String WEB_READY = "webReady";

    // 생성/검증/루프
    public static final String MAX_ITEMS = "maxItems";
    public static final String DRAFT = "draft";
    public static final String ERRORS = "errors";// List<String>

    // 공통 변수
    public static final String BASEVARS = "baseVars";

    // ---- SCHEMA: base vs appender 구분 ----
    // 각 키와 채널(Channel)의 매핑 정의
    // 이때 `Channels.appender(...)`: 리스트처럼 누적(append)할 때 사용.

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력/메타 (덮어쓰기)
            Map.entry(CORP_CODE, Channels.base(() -> "")),
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),
            // 선택 소스는 1회만 쓰면 base, 동적 누적이면 appender로 바꿔도 됨
            Map.entry(SOURCES, Channels.base(() -> new ArrayList<String>())),
            Map.entry(FINANCIALS, Channels.base(() -> "")),

            // 병렬 수집용 리스트 채널 (appender)
            Map.entry(WEB_DOCS, Channels.appender(ArrayList<WebDocs>::new)),
            Map.entry(DB_DOCS, Channels.appender(ArrayList<DbDocDto>::new)),

            // 소스별 실행 완료 여부
            Map.entry(DB_READY, Channels.base(() -> false)),
            Map.entry(WEB_READY, Channels.base(() -> false)),

            // 생성/검증/루프 (덮어쓰기 + 피드백은 누적)
            Map.entry(MAX_ITEMS, Channels.base(() -> "")),
            Map.entry(DRAFT, Channels.appender(ArrayList::new)),
            Map.entry(ERRORS, Channels.appender(ArrayList::new)),

            Map.entry(BASEVARS, Channels.base(() -> Map.of()))
    );


    public DraftState(Map<String, Object> init) {
        super(init);
    }

    // ---- Getter ----
    public String getCorpCode() { return this.<String>value(CORP_CODE).orElse(""); }
    public String getCorpName() { return this.<String>value(CORP_NAME).orElse(""); }
    public String getIndutyCode() { return this.<String>value(IND_CODE).orElse(""); }
    public String getIndutyName() { return this.<String>value(IND_NAME).orElse(""); }
    public String getSection() { return this.<String>value(SECTION).orElse(""); }
    public String getSectionLabel() { return this.<String>value(SECTION_LABEL).orElse(""); }
    public List<String> getSources() { return this.<List<String>>value(SOURCES).orElse(Collections.emptyList()); }
    public String getFinancials() { return this.<String>value(FINANCIALS).orElse(""); }

    public List<WebDocs> getWebDocs() { return this.<List<WebDocs>>value(WEB_DOCS).orElse(new ArrayList<WebDocs>()); }
    public List<DbDocDto> getDbDocs() { return this.<List<DbDocDto>>value(DB_DOCS).orElse(new ArrayList<DbDocDto>()); }

    public boolean isDbReady() { return this.<Boolean>value(DB_READY).orElse(false); }
    public boolean isWebReady() { return this.<Boolean>value(WEB_READY).orElse(false); }

    public String getMaxItems() { return this.<String>value(MAX_ITEMS).orElse("5"); }
    public List<String> getDrafts() { return this.<List<String>>value(DRAFT).orElse(List.of()); }
    public List<String> getErrors() { return this.<List<String>>value(ERRORS).orElse(new ArrayList<>()); }

    public Map<String, Object> getBaseVars() { return this.<Map<String, Object>>value(BASEVARS).orElse(Collections.emptyMap()); }
}

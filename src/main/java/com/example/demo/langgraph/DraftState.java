package com.example.demo.langgraph;

import com.example.demo.dto.ContextDoc;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

public class DraftState extends AgentState {

    // ---- 키 상수 ----
    public static final String CORP_CODE = "corpCode";
    public static final String CORP_NAME = "corpName";
    public static final String IND_CODE = "indutyCode";
    public static final String IND_NAME = "indutyName";
    public static final String SECTION = "section";
    public static final String SECTION_LABEL = "sectionLabel";
    public static final String PROMPT = "prompt";
    public static final String SOURCES = "sources"; // ex) ["web","news","db"]

    // 소스별 컨텍스트 (병렬 수집 → 반드시 appender)
    public static final String WEB_DOCS = "webDocs";   // List<Doc>
    public static final String NEWS_DOCS = "newsDocs";  // List<Doc>
    public static final String DB_DOCS = "dbDocs";    // List<Doc>
    public static final String CONTEXT = "context"; // fan-in 결과 (List<Doc>)

    // 생성/검증/루프
    public static final String MAX_ITEMS = "maxItems";
    public static final String DRAFT = "draft";
    public static final String ERRORS = "errors"; // List<String>

    // ---- SCHEMA: base vs appender 구분 ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력/메타 (덮어쓰기)
            Map.entry(CORP_CODE, Channels.base(() -> "")),
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),
            Map.entry(PROMPT, Channels.base(() -> "")),
            // 선택 소스는 1회만 쓰면 base, 동적 누적이면 appender로 바꿔도 됨
            Map.entry(SOURCES, Channels.base(() -> new ArrayList<String>())),

            // 병렬 수집용 리스트 채널 (appender)
            Map.entry(WEB_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(NEWS_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(DB_DOCS, Channels.appender(ArrayList::new)),
            Map.entry(CONTEXT, Channels.base(() -> new ArrayList<ContextDoc>())),

            // 생성/검증/루프 (덮어쓰기 + 피드백은 누적)
            Map.entry(MAX_ITEMS, Channels.base(() -> "")),
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

    public List<String> dbDocs() {
        return this.<List<String>>value(DB_DOCS).orElse(List.of());
    }
}

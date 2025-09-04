package com.example.demo.dbsubgraph.state;

import com.example.demo.dto.dbsubgraph.DbDocDto;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbSubGraphState extends AgentState {

    // ---- 키 상수 ----
    public static final String SECTION   = "sectionName";
    public static final String CORP_CODE = "corpCode";
    public static final String IND_CODE = "indutyCode";
    public static final String IND_NAME = "indutyName";
    public static final String FINANCIALS = "financials";
    public static final String FILTER_CRITERIA = "filterCriteria";
    public static final String LABEL = "label";
    public static final String PEER_CODES = "peerCorps";
    public static final String RAW_DOCS = "rawDocs";
    public static final String DB_DOCS = "dbDocs"; // 최종 출력 (DraftState와 동일한 키)

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(CORP_CODE, Channels.base(() -> "")),
            Map.entry(IND_CODE, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
//            Map.entry(FINANCIALS, Channels.base(() -> "")),
            Map.entry(FILTER_CRITERIA, Channels.base(() -> "")),
            Map.entry(LABEL, Channels.base(() -> "")),
            Map.entry(PEER_CODES, Channels.base(ArrayList::new)),
            Map.entry(RAW_DOCS, Channels.base(ArrayList::new)),
            Map.entry(DB_DOCS, Channels.appender(ArrayList<DbDocDto>::new))
    );

    // 생성자
    public DbSubGraphState(Map<String, Object> init) {
        super(init);
    }

    // Getter 메서드 (선택적)
    public String sectionName() { return this.<String>value(SECTION).orElse(""); }
    public String corpCode() { return this.<String>value(CORP_CODE).orElse(""); }
    public String indCode() { return this.<String>value(IND_CODE).orElse(""); }
    public String indName() { return this.<String>value(IND_NAME).orElse(""); }
    //    public String financials() { return this.<String>value(FINANCIALS).orElse(""); }
    public List<DbDocDto> dbDocs() { return this.<List<DbDocDto>>value(DB_DOCS).orElse(new ArrayList<>()); }
}

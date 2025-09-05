package com.example.demo.validatorgraph;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

public class ValidatorState extends AgentState {

    // ---- 키 상수 ----
    public static final String IND_NAME = "indutyName";
    public static final String SECTION = "section";
    public static final String SECTION_LABEL = "sectionLabel";
    public static final String METHOD = "method"; // draft 수정/검토기능
    // 생성/검증/루프
    public static final String DRAFT = "draft";
    public static final String GUIDE_INDEX = "guideIndex";
    public static final String GUIDE_HITS = "guideHits"; // List<Map>
    public static final String VALIDATION = "validation";
    public static final String ADJUST_INPUT = "adjustInput";
    public static final String DECISION = "decision";
    public static final String ERRORS = "errors"; // List<String>

    // ---- SCHEMA: base vs appender 구분 ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            // 입력/메타 (덮어쓰기)
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(SECTION_LABEL, Channels.base(() -> "")),
            Map.entry(METHOD, Channels.base(() -> "")),
            // 생성/검증/루프 (덮어쓰기 + 피드백은 누적)
            Map.entry(DRAFT, Channels.appender(ArrayList::new)),
            Map.entry(GUIDE_INDEX, Channels.base(() -> "")),
            Map.entry(GUIDE_HITS, Channels.appender(ArrayList::new)),
            Map.entry(VALIDATION, Channels.base(() -> new HashMap<>())),
            Map.entry(ADJUST_INPUT, Channels.appender(ArrayList::new)),
            Map.entry(DECISION, Channels.base(() -> "")),
            Map.entry(ERRORS, Channels.appender(ArrayList::new))
    );
    public ValidatorState(Map<String, Object> initData) {
        super(initData);
    }
}

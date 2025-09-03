package com.example.demo.langgraph.web.state; //파일이 속한 **패키지(폴더 구조)**를 정의

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.*;

public class WebGraph extends AgentState {

    // ---- 키 상수 ----
    public static final String SECTION   = "sectionName";
    public static final String CORP_NAME = "corpName";
    public static final String IND_NAME  = "industryName";

    // ---- SCHEMA ----
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry(SECTION, Channels.base(() -> "")),
            Map.entry(CORP_NAME, Channels.base(() -> "")),
            Map.entry(IND_NAME, Channels.base(() -> "")),
            Map.entry("queries", Channels.base(() -> List.of()))
    );

    // ---- 필드 ----
    private String corpName;   // 회사명
    private String industry;   // 산업명
    private List<String> queries; // 검색 쿼리들

    public WebGraph(Map<String, Object> initData) {
        super(initData);
    }
}

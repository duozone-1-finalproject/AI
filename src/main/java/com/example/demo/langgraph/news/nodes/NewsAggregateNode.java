package com.example.demo.langgraph.nodes;

import com.example.demo.dto.SummaryDto;
import com.example.demo.dto.ReportDto;
import com.example.demo.dto.ThemeDto;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.*;
import java.util.stream.Collectors;

public class NewsAggregateNode implements NodeAction<AgentState> {

    private static final Map<String, List<String>> THEME_KEYWORDS = Map.of(
            "투자/증설", List.of("투자", "증설", "합작", "M&A", "CAPEX"),
            "공급망/수급", List.of("공급망", "수급", "원자재", "조달", "납품", "리드타임"),
            "규제/정책", List.of("규제", "정책", "심사", "허가", "개정", "완화", "강화"),
            "실적/수요", List.of("실적", "매출", "수요", "가이던스", "전망", "성장"),
            "리스크/분쟁", List.of("리스크", "논란", "제재", "소송", "분쟁", "리콜")
    );

    @Override
    public AgentState apply(AgentState state, Map<String,Object> ctx) {
        List<SummaryDto> summaries = state.getList("summaries", SummaryDto.class);

        // 테마별 카운트 집계
        Map<String, List<SummaryDto>> grouped = new HashMap<>();
        for (SummaryDto s : summaries) {
            for (var entry : THEME_KEYWORDS.entrySet()) {
                boolean matched = entry.getValue().stream()
                        .anyMatch(k -> s.getSummary().toString().contains(k) || s.getKeyPoints().contains(k));
                if (matched) {
                    grouped.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(s);
                }
            }
        }

        // ReportDto 생성
        List<ThemeDto> themes = grouped.entrySet().stream()
                .map(e -> new ThemeDto(e.getKey(), e.getValue().size(),
                        e.getValue().stream().map(SummaryDto::getLink).collect(Collectors.toList())))
                .toList();

        ReportDto report = new ReportDto();
        report.setThemes(themes);
        report.setSentimentOverall("neutral"); // TODO: sentiment 평균내서 세팅
        report.setNotes("자동 생성된 뉴스 요약 리포트");

        state.set("report", report);
        return state;
    }
}

package com.example.demo.langgraph.nodes;

import com.example.demo.langgraph.state.RiskState;
import com.example.demo.service.ElasticsearchService;
import org.bsc.langgraph4j.action.NodeAction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ValidateAgainstStandardNode implements NodeAction<RiskState> {
    private final ElasticsearchService es;
    private final String keyword;

    public ValidateAgainstStandardNode(ElasticsearchService es, String keyword) {
        this.es = es; this.keyword = keyword;
    }

    @Override
    public Map<String, Object> apply(RiskState state) {
        String draft = state.draft();
        try {
            // '투자위험' 관련 작성기준 조항 검색
            List<Map<String,Object>> standards = es.searchStandardByKeyword(keyword, 3);
            String ruleDigest = standards.stream()
                    .map(m -> String.valueOf(m.getOrDefault("content","")))
                    .reduce("", (a,b) -> a + "\n- " + b);

            String validated = draft + "\n\n[작성기준 검토 체크]\n" + ruleDigest;
            return Map.of(RiskState.FINAL, validated);
        } catch (IOException e) {
            return Map.of(RiskState.FINAL, draft + "\n\n[작성기준 검토 체크] (조회 실패로 스킵)");
        }
    }
}

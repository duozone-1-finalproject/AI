package com.example.demo.service;

import com.example.demo.config.AiSectionProperties;
import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

    private final CompiledGraph<DraftState> graph;
    private final AiSectionProperties aiSectionProperties;

    @Override
    public DraftResponseDto run(DraftRequestDto req) {
        DraftResponseDto dto = new DraftResponseDto();
        dto.setRiskIndustry(runOne("risk_industry", req));
        dto.setRiskCompany(runOne("risk_company", req));
        dto.setRiskEtc(runOne("risk_etc", req));
        return dto;
    }

    private String runOne(String sectionKey, DraftRequestDto req) {
        // 설정에서 섹션 레이블(이름) 조회
        String sectionLabel = aiSectionProperties.getSections().get(sectionKey).getLabel();

        // 그래프 초기 상태 구성
        Map<String, Object> init = new LinkedHashMap<>();
        init.put(DraftState.CORP_CODE, req.getCorpCode());
        init.put(DraftState.CORP_NAME, req.getCorpName());
        init.put(DraftState.IND_CODE, req.getIndutyCode());
        init.put(DraftState.IND_NAME, req.getIndutyName());
        init.put(DraftState.RPT_EXIST, Boolean.TRUE.equals(req.getRptExist()));
        init.put(DraftState.SECTION, sectionKey);
        init.put(DraftState.SECTION_LABEL, sectionLabel);

        // 그래프 실행 → 최종 상태에서 초안 텍스트 추출
        DraftState finalState = graph.invoke(init).orElse(new DraftState(Map.of()));  // compile()된 그래프는 invoke/stream 가능
        return finalState.<List<String>>value(DraftState.DRAFT).orElseThrow().getFirst();
    }
}

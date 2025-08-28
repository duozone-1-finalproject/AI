package com.example.demo.service;

import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.langgraph.state.DraftState;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import static com.example.demo.langgraph.state.DraftState.DRAFT;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphServiceImpl implements GraphService {

    private final CompiledGraph<DraftState> graph;

    public GraphServiceImpl(CompiledGraph<DraftState> graph) {
        this.graph = graph;
    }

//    @Override
//    public LangGraphDto.GraphResult run(LangGraphDto.PromptRequest promptRequest) {
//        var resultState = graph.invoke(Map.of("input", promptRequest)).orElse(new AgentState(Map.of()));
//        return resultState.value("output")
//                .map(LangGraphDto.GraphResult.class::cast)
//                .orElse(new LangGraphDto.GraphResult(""));
//    }

    @Override
    public DraftResponseDto run(DraftRequestDto req) {
        DraftResponseDto dto = new DraftResponseDto();
        dto.setRiskIndustry(runOne("risk_industry", req));
        dto.setRiskCompany(runOne("risk_company", req));
        dto.setRiskEtc(runOne("risk_etc", req));
        return dto;
    }

    private String runOne(String sectionKey, DraftRequestDto req) {
        // 그래프 초기 상태 구성
        Map<String, Object> init = new LinkedHashMap<>();
        init.put(DraftState.CORP_CODE,  req.getCorpCode());
        init.put(DraftState.CORP_NAME,  req.getCorpName());
        init.put(DraftState.IND_CODE,   req.getIndutyCode());
        init.put(DraftState.IND_NAME,   req.getIndutyName());
        init.put(DraftState.RPT_EXIST,  Boolean.TRUE.equals(req.getRptExist()));
        init.put(DraftState.SECTION,    sectionKey);

        // 그래프 실행 → 최종 상태에서 초안 텍스트 추출
        DraftState finalState = graph.invoke(init).orElse(new DraftState(Map.of()));  // compile()된 그래프는 invoke/stream 가능
        return finalState.<List<String>>value(DraftState.DRAFT).orElseThrow().getFirst();
    }
}

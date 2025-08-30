package com.example.demo.service;

import com.example.demo.config.AiSectionProperties;
import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.langgraph.state.DraftState;
import lombok.RequiredArgsConstructor;
import org.bsc.async.AsyncGenerator;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
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
        String sectionLabel = aiSectionProperties.getSections().get(sectionKey).getLabel();

        Map<String, Object> init = new LinkedHashMap<>();
        init.put(DraftState.CORP_CODE, req.getCorpCode());
        init.put(DraftState.CORP_NAME, req.getCorpName());
        init.put(DraftState.IND_CODE, req.getIndutyCode());
        init.put(DraftState.IND_NAME, req.getIndutyName());
        init.put(DraftState.RPT_EXIST, Boolean.TRUE.equals(req.getRptExist()));
        init.put(DraftState.SECTION, sectionKey);
        init.put(DraftState.SECTION_LABEL, sectionLabel);

        AsyncGenerator<NodeOutput<DraftState>> stream = graph.stream(init);

        final AtomicReference<DraftState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            DraftState currentState = nodeOutput.state();
            log.debug("Graph node processed. Current state: {}", currentState);
            finalStateRef.set(currentState);
        });

        DraftState finalState = finalStateRef.get();
        if (finalState == null) {
            // 스트림이 비어있는 경우에 대한 처리
            finalState = new DraftState(Map.of());
        }

        return finalState.<List<String>>value(DraftState.DRAFT).orElseThrow().getLast();
    }
}

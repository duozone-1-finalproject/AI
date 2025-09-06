package com.example.demo.graphweb.service;

import com.example.demo.dto.graphweb.WebRequestDto;
import com.example.demo.dto.graphweb.WebResponseDto;
import com.example.demo.graphweb.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebService {

    private final CompiledGraph<WebState> graph;

// @bean은 초반에 @data, @service등 여러 어노테이션으로 설정해둔 객체들이 한 박스에 들어갔다고 생각
    // 이때, 알맞은 객체를 자동으로 불러오는 것임. new로 새로운 객체를 만들지 않아도 됨
    // 참고로 public은 전역 변수이고 private는 다른거 못불러오게 그 안에서만 쓸 수 있는 것.

    public WebResponseDto run(WebRequestDto req) {
        // 초기 state 구성
        Map<String, Object> init = new LinkedHashMap<>();
        init.put(WebState.CORP_NAME, req.getCorpName());
        init.put(WebState.IND_NAME, req.getIndustryName());
        init.put(WebState.IND_CODE, req.getIndustryCode());

        // 그래프 실행
        WebState finalState = graph.invoke(init)
                .orElse(new WebState(Map.of()));

        // 결과를 DTO로 변환
        WebResponseDto dto = new WebResponseDto();
        dto.setSummaries(finalState.value(WebState.SUMMARIES).orElse(null));
        dto.setValidated(finalState.value(WebState.VALIDATED).orElse(null));

        return dto;
    }

    public String search(String q) {
    }
}




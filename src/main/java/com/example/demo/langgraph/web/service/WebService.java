package com.example.demo.langgraph.web.service;

import com.example.demo.dto.WebRequestDto;
import com.example.demo.dto.WebResponseDto;
import com.example.demo.langgraph.web.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebService {

    private final DuckService duckService;
    private final CompiledGraph<WebState> webGraph;

    public WebResponseDto run(WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState state = new WebState(initData);
        state = webGraph.run(state);

        // 결과를 DTO로 변환
        WebResponseDto response = new WebResponseDto();
        response.setQueries((List<String>) state.value(WebState.QUERY).orElse(null));
        response.setArticles((List<String>) state.value(WebState.ARTICLES).orElse(null));
        response.setSummaries((List<String>) state.value(WebState.SUMMARIES).orElse(null));

        return response;
    }
}





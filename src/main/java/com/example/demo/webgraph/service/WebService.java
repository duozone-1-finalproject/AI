package com.example.demo.webgraph.service;

import com.example.demo.dbsubgraph.state.DbSubGraphState;
import com.example.demo.dto.WebRequestDto;
import com.example.demo.dto.WebResponseDto;
import com.example.demo.webgraph.state.WebState;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebService {

    private final CompiledGraph<WebState> webGraph;

    public WebResponseDto run(WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));  ;

        // 결과를 DTO로 변환
        WebResponseDto response = new WebResponseDto();
        response.setQueries(resultState.getQueries());
        response.setArticles(resultState.getArticles());
        response.setSummaries(resultState.getSummaries());

        return response;
    }
}





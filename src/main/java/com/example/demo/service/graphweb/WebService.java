package com.example.demo.service.graphweb;

import com.example.demo.dto.graphweb.WebRequestDto;
import com.example.demo.dto.graphweb.WebResponseDto;
import com.example.demo.graphweb.WebState;
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
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));

        // [수정] 그래프 실행 결과(resultState)에서 최종 데이터를 추출하여 응답 DTO를 생성합니다.
        WebResponseDto response = new WebResponseDto();
        response.setCandidates(resultState.getFinalResult()); // 최종 검증 통과한 기사 목록
        response.setErrors(resultState.getErrors()); // 에러 로그

        return response; // 완성된 응답 객체를 반환합니다.
    }
}

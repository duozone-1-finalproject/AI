package com.example.demo.controller;

import com.example.demo.dto.SearchLLMDto;
import com.example.demo.dto.WebRequestDto;
import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.graphweb.service.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/duck")
public class WebController {
    @Qualifier("chatWithMcp")
    private final ChatClient chatClient;
    private final WebService webService;
    private final CompiledGraph<WebState> webGraph;

    // ✅ 사용자 입력은 corpName, indutyName, section만 받음.
    // q(검색어)는 내부 QueryBuilderNode에서 자동 생성.
    @PostMapping("/search")
    public WebResponseDto search(@RequestBody WebRequestDto req) {
        log.info("Received web request: {}", req);
        return webService.run(req);
    }

    @GetMapping("/test")
    public String test_ddg(@RequestParam String q) {
        return chatClient.prompt(q).call().content();
    }

    @PostMapping("/tt")
    public List<SearchLLMDto> tt(@RequestBody WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));

        return resultState.getArticles(); // 완성된 응답 객체를 반환합니다.
    }
}




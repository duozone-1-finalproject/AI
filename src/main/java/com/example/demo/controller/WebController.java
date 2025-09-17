
package com.example.demo.controller;

import com.example.demo.dto.graphweb.SearchLLMDto;
import com.example.demo.dto.graphweb.WebDocs;
import com.example.demo.dto.graphweb.WebRequestDto;
import com.example.demo.dto.graphweb.WebResponseDto;
import com.example.demo.graphweb.WebState;
import com.example.demo.service.graphweb.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

    @PostMapping("/fetch")
    public String fetch(@RequestBody WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));

        return resultState.getFetchedArticles(); // 완성된 응답 객체를 반환합니다.
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

    @PostMapping("/val")
    public Boolean val(@RequestBody WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));

        return resultState.getValidated(); // 완성된 응답 객체를 반환합니다.
    }

    @PostMapping("/pre")
    public String pre(@RequestBody WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());

        // 그래프 실행
        WebState resultState = webGraph.invoke(initData).orElse(new WebState(Map.of()));

        return resultState.getProArticles(); // 완성된 응답 객체를 반환합니다.
    }

    @PostMapping("/res")
    public List<WebDocs> res(@RequestBody WebRequestDto req) {
        // 초기 상태 데이터 준비
        Map<String, Object> initData = new HashMap<>();
        initData.put(WebState.CORP_NAME, req.getCorpName());
        initData.put(WebState.IND_NAME, req.getIndutyName());
        initData.put(WebState.SECTION_LABEL, req.getSectionLabel());
        webGraph.setMaxIterations(100);

        log.info("################################ WEB GRAPH START ##########################################");

        AsyncGenerator<NodeOutput<WebState>> stream = webGraph.stream(initData);

        final AtomicReference<WebState> finalStateRef = new AtomicReference<>();

        stream.forEach(nodeOutput -> {
            WebState currentState = nodeOutput.state();
            log.info("Web Graph node processed. Current node: {}", nodeOutput.node());
            log.debug("WebState SearchNode: \n{}", currentState.getArticles());
            log.info("WebState WebDocs(size={}): {}", currentState.getWebDocs().size(), currentState.getWebDocs());
            finalStateRef.set(currentState);
        });

        WebState finalState = finalStateRef.get();
        if (finalState == null) {
            // 스트림이 비어있는 경우에 대한 처리
            finalState = new WebState(Map.of());
        }
        log.info("################################ WEB GRAPH END ##########################################");
        return finalState.getWebDocs();
    }
}



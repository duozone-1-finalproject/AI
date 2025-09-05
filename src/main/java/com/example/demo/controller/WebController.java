//# 웹 서브그래프 호출용 (DuckDuckGo MCP)
package com.example.demo.controller;

import com.example.demo.dto.WebRequestDto;
import com.example.demo.dto.WebResponseDto;
import com.example.demo.langgraph.web.service.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/duck")
public class WebController {

    private final WebService webService;

    // ✅ 사용자 입력은 corpName, indutyName, section만 받음.
// q(검색어)는 내부 QueryBuilderNode에서 자동 생성.
    @PostMapping("/search")
    public WebResponseDto search(@RequestBody WebRequestDto req) {
        return webService.run(req);
    }
}

@RestController
@RequiredArgsConstructor
public class DemoController {

    @Qualifier("chatPlain")
    private final ChatClient chatPlain;

    @Qualifier("chatWithMcp")
    private final ChatClient chatWithMcp;

    @GetMapping("/plain")
    public String plain(@RequestParam(defaultValue = "간단 자기소개") String q) {
        return chatPlain.prompt(q).call().content();
    }

    @GetMapping("/mcp")
    public String mcp(@RequestParam(defaultValue = "OpenSearch 최신 변화 요약(출처 포함)") String q) {
        // 필요 시 LLM이 MCP 툴을 호출
        return chatWithMcp.prompt(q).call().content();
    }
}





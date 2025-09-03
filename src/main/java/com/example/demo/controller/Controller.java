package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {

    @Qualifier("default")
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

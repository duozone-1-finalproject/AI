// 웹 서브그래프 호출용 (DuckDuckGo MCP)
// DuckDuckGo 검색 + 내부 로직 결과 반환용 API

package com.example.demo.controller;

import com.example.demo.dto.WebRequestDto;
import com.example.demo.dto.WebResponseDto;
import com.example.demo.graphweb.service.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/duck")
public class WebController {

    private final WebService webService;

    // ✅ 사용자 입력은 corpName, indutyName, section만 받음.
// q(검색어)는 내부 QueryBuilderNode에서 자동 생성.
    @PostMapping("/search")
    public WebResponseDto search(@RequestBody WebRequestDto req) {
        log.info("Received web request: {}", req);
        return webService.run(req);
    }
}




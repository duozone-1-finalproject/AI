//# 웹 서브그래프 호출용 (DuckDuckGo MCP)
package com.example.demo.controller;

import com.example.demo.langgraph.web.service.DuckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/duck")
public class DuckController {

    private final DuckService duckService;

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "삼성전자 산업 리스크") String q) {
        return duckService.search(q);
    }
}




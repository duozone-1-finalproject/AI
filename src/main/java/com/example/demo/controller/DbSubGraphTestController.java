package com.example.demo.controller;

import com.example.demo.graphdb.nodes.TestDbSubgraphinvoker;
import com.example.demo.dto.graphmain.DraftRequestDto;
import com.example.demo.dto.graphdb.DbDocDto;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dbSubGraph")
@RequiredArgsConstructor
public class DbSubGraphTestController {
    private final TestDbSubgraphinvoker graphService;

    @PostMapping
    public List<DbDocDto> dbContent(@Valid @RequestBody DraftRequestDto req) {
        // 디버그 출력
        log.debug("DraftRequestDto: {}", req);
        return graphService.runOne("risk_etc", req);
    }

    @PostMapping("/financials")
    public String draftFinancials(@Valid @RequestBody DraftRequestDto req) {
        // 디버그 출력
        log.debug("DraftRequestDto: {}", req);
        return graphService.runTest("risk_company", req);
    }
}

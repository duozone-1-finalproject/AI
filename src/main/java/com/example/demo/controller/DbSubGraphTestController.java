package com.example.demo.controller;

import com.example.demo.dbsubgraph.nodes.TestDbSubgraphinvoker;
import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.dbsubgraph.DbDocDto;
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
    public List<DbDocDto> draft(@Valid @RequestBody DraftRequestDto req) {
        // 디버그 출력
        log.debug("DraftRequestDto: {}", req);
        return graphService.runOne("risk_industry", req);
    }
}

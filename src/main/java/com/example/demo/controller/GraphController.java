package com.example.demo.controller;

import com.example.demo.dto.graphmain.DraftRequestDto;
import com.example.demo.dto.graphmain.DraftResponseDto;
import com.example.demo.service.graphmain.GraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @PostMapping
    public DraftResponseDto draft(@Valid @RequestBody DraftRequestDto req) {
        // 2. 로그 출력 코드 추가
        log.info("Received draft request: {}", req);
        // 또는 디버그 레벨로 출력
        log.debug("Received draft request: {}", req);
        return graphService.run(req);
    }
}

package com.example.demo.controller;

import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.service.GraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

//    @PostMapping(path = "/run", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public LangGraphDto.GraphResult run(@Valid @RequestBody LangGraphDto.PromptRequest promptRequest) {
//        return graphService.run(req);
//    }

    @PostMapping
    public DraftResponseDto draft(@Valid @RequestBody DraftRequestDto req) {
        return graphService.run(req);
    }
}

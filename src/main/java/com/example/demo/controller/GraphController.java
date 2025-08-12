package com.example.demo.controller;

import com.example.demo.dto.LangGraphDto;
import com.example.demo.service.GraphService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping(path = "/run", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LangGraphDto.GraphResult run(@Valid @RequestBody LangGraphDto.PromptRequest promptRequest) {
        return graphService.run(promptRequest);
    }
}

package com.example.demo.controller;

import com.example.demo.dto.RiskRequest;
import com.example.demo.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {
    private final RiskService riskService;

    @PostMapping(value="/generate", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String,String>> generate(@RequestBody RiskRequest req) throws Exception {
        return riskService.generate(req);
    }
}
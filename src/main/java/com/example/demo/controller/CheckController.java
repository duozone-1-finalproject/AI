package com.example.demo.controller;

import com.example.demo.dto.graphvalidator.CheckRequestDto;
import com.example.demo.dto.graphvalidator.ValidationDto;
import com.example.demo.service.graphvalidator.CheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CheckController {
    private final CheckService checkService;

    @PostMapping("/check")
    public ValidationDto check(@Valid @RequestBody CheckRequestDto req) {
        // 2. 로그 출력 코드 추가
        log.info("Received check request: {}", req);
        // 또는 디버그 레벨로 출력
//        log.debug("Received check request: {}", req);
        return checkService.check(req);
    }



    @PostMapping("/revise")
    public String revise(@Valid @RequestBody ValidationDto.Issue req) {
        // 2. 로그 출력 코드 추가
        log.info("Received revise request: {}", req);
        // 또는 디버그 레벨로 출력
//        log.debug("Received revise request: {}", req);
        return checkService.revise(req);
    }
}

package com.example.demo.service;

import com.example.demo.dto.CheckRequestDto;
import com.example.demo.dto.ValidationDto;
import java.util.List;

public interface CheckService {
    List<ValidationDto.Issue> check(CheckRequestDto requestDto);
    List<String> draftValidate(CheckRequestDto requestDto);
}

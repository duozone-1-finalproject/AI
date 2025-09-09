package com.example.demo.service.graphvalidator;

import com.example.demo.dto.graphvalidator.CheckRequestDto;
import com.example.demo.dto.graphvalidator.ValidationDto;
import java.util.List;

public interface CheckService {
    ValidationDto check(CheckRequestDto requestDto);
    List<String> draftValidate(CheckRequestDto requestDto);
    String revise(ValidationDto.Issue requestDto);
}

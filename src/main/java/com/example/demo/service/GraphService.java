package com.example.demo.service;

import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;
import com.example.demo.dto.LangGraphDto;

public interface GraphService {
//    LangGraphDto.GraphResult run(LangGraphDto.PromptRequest promptRequest);
    DraftResponseDto run(DraftRequestDto request);
}

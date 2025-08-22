package com.example.demo.service;

import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;

public interface GraphService {
//    LangGraphDto.GraphResult run(LangGraphDto.PromptRequest promptRequest);
    DraftResponseDto run(DraftRequestDto request);
}

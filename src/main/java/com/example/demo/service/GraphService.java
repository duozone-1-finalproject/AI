package com.example.demo.service;

import com.example.demo.dto.LangGraphDto;

public interface GraphService {
    LangGraphDto.GraphResult run(LangGraphDto.PromptRequest promptRequest);
}

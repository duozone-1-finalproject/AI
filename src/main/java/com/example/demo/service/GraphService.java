package com.example.demo.service;

import com.example.demo.dto.DraftRequestDto;
import com.example.demo.dto.DraftResponseDto;

public interface GraphService {
    DraftResponseDto run(DraftRequestDto request);
}

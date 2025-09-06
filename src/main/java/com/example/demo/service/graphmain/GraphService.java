package com.example.demo.service.graphmain;

import com.example.demo.dto.graphmain.DraftRequestDto;
import com.example.demo.dto.graphmain.DraftResponseDto;

public interface GraphService {
    DraftResponseDto run(DraftRequestDto request);
}

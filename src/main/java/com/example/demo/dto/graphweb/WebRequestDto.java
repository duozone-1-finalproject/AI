package com.example.demo.dto.graphweb;

import lombok.Data;

@Data
public class WebRequestDto {
    private String corpName; // 기업명
    private String indutyName; // 산업명
    private String indutyCode; // 산업 코드
    private String sectionLabel; // "산업위험" or "회사위험"
}
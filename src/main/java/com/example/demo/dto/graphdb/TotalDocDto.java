package com.example.demo.dto.graphdb;

import lombok.Data;

import java.util.List;

@Data
public class TotalDocDto {
    private String corp_code;
    private List<RawDocDto> sections;
}

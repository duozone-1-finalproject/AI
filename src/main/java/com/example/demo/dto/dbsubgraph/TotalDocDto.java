package com.example.demo.dto.dbsubgraph;

import lombok.Data;

import java.util.List;

@Data
public class TotalDocDto {
    private String corp_code;
    private List<RawDocDto> sections;
}

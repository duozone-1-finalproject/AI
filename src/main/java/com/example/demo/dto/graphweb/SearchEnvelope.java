package com.example.demo.dto.graphweb;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SearchEnvelope implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<SearchLLMDto> items;
}
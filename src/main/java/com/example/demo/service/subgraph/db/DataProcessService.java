package com.example.demo.service.subgraph.db;

import com.example.demo.dto.dbsubgraph.DbDocDto;

import java.util.List;

public interface DataProcessService {
    List<DbDocDto> processData(List<String> rawDocs);
}

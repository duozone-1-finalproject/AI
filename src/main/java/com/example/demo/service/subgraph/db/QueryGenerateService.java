package com.example.demo.service.subgraph.db;

import com.example.demo.dto.dbsubgraph.QueryRequestDto;

import java.io.IOException;

public interface QueryGenerateService {
    QueryRequestDto generateQuery(String filterCriteria, String corpCode, String indCode) throws IOException;
}

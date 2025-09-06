package com.example.demo.service.graphdb;

import com.example.demo.dto.graphdb.QueryRequestDto;

import java.io.IOException;

public interface QueryGenerateService {
    QueryRequestDto generateQuery(String filterCriteria, String corpCode, String indCode) throws IOException;
}

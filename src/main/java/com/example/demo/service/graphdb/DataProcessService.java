package com.example.demo.service.graphdb;

import com.example.demo.dto.graphdb.DbDocDto;

import java.util.List;

public interface DataProcessService {
    List<DbDocDto> processData(List<String> rawDocs);
}

package com.example.demo.service.subgraph.db.impl;

import com.example.demo.dto.dbsubgraph.DbDocDto;
import com.example.demo.service.subgraph.db.DataProcessService;
import com.example.demo.util.dbsubgraph.ParsingUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataProcessServiceImpl implements DataProcessService {
    @Override
    public List<DbDocDto> processData(List<String> rawDocs) {
        List<DbDocDto> results = new ArrayList<>();

        for (int i = 0; i < rawDocs.size(); i++) {
            DbDocDto dto = new DbDocDto();
            dto.setId(String.valueOf(i));
            dto.setSec_content(ParsingUtils.extractItems(rawDocs.get(i)));
            results.add(dto);
        }

        return results;
    }
}

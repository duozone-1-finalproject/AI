package com.example.demo.service.graphdb.impl;

import com.example.demo.dto.graphdb.QueryRequestDto;
import com.example.demo.service.graphdb.FinancialDataService;
import com.example.demo.service.graphdb.QueryGenerateService;
import com.example.demo.service.graphdb.QueryGenerator;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryGenerateServiceImpl implements QueryGenerateService {

    private final Map<String, QueryGenerator> generators;
    private final FinancialDataService financialDataService;

    @Override
    public QueryRequestDto generateQuery(String filterCriteria, String corpCode, String indCode) throws IOException {
        QueryGenerator generator = switch (filterCriteria) {
            case "financial" -> generators.get("financialQueryGenerator");
            case "industry"  -> generators.get("industryQueryGenerator");
            case "latest"    -> generators.get("latestQueryGenerator");
            default -> throw new IllegalArgumentException("Unknown filterCriteria");
        };

        Map<String, Object> extraParams = switch (filterCriteria) {
            case "financial" -> financialDataService.getFinancialData(corpCode);
            case "industry"  -> Map.of("ind_code", indCode);
            default          -> Map.of();
        };

        Query query = generator.generate(corpCode, extraParams);

        // indexName 결정
        String indexName = switch (filterCriteria) {
            case "financial" -> "fin_index";
            default          -> "rpt_sec_eq";
        };

        return new QueryRequestDto(query, indexName);
    }
}

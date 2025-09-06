package com.example.demo.service.graphdb;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.util.Map;

public interface QueryGenerator {
    Query generate(String corpCode, Map<String, Object> extraParams);
}

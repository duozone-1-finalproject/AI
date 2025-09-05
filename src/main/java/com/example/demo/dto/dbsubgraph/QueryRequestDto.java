package com.example.demo.dto.dbsubgraph;

import org.opensearch.client.opensearch._types.query_dsl.Query;

public record QueryRequestDto(Query query, String indexName) {}

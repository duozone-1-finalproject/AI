package com.example.demo.service.subgraph.db;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DbSubGraphService {
    List<String> fetchPeerCorpCodes(Query query, String indexName) throws IOException;
    List<String> fetchReportSections(List<String> corpCodes, String sectionTitle) throws IOException;
    List<Map<String, Object>> fetchStandardAccounts(String corpCode) throws IOException;
}
package com.example.demo.service.graphdb;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DbSubGraphService {
    List<String> findPeerCompanies(Query query, String indexName) throws IOException;
    List<String> getReportSections(List<String> corpCodes, String sectionTitle) throws IOException;
    List<Map<String, Object>> getStandardFinancialAccounts(String corpCode) throws IOException;
    Map<String, Map<String, Double>> getAllFinancialAccounts(String corpCode) throws IOException;
}
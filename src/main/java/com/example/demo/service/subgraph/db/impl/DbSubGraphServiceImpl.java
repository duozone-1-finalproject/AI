package com.example.demo.service.subgraph.db.impl;

import com.example.demo.repository.OpenSearchRepository;
import com.example.demo.service.subgraph.db.DbSubGraphService;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DbSubGraphServiceImpl implements DbSubGraphService {

    private final OpenSearchRepository openSearchRepository;


    @Override
    public List<String> fetchPeerCorpCodes(Query query, String indexName) throws IOException {
        return openSearchRepository.findPeerCorpCodes(query, indexName);
    }

    @Override
    public List<String> fetchReportSections(List<String> corpCodes, String sectionTitle) throws IOException {
        return openSearchRepository.fetchSectionContents(corpCodes, sectionTitle);
    }

    @Override
    public List<Map<String, Object>> fetchStandardAccounts(String corpCode) throws IOException {
        return openSearchRepository.fetchStandardAccounts(corpCode);
    }
}

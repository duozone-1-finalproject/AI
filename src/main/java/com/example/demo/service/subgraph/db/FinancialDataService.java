package com.example.demo.service.subgraph.db;

import java.io.IOException;
import java.util.Map;

public interface FinancialDataService {
    Map<String, Object> getFinancialData(String corpCode) throws IOException;
}

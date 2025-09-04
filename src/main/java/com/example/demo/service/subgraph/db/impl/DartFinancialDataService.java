package com.example.demo.service.subgraph.db.impl;

import com.example.demo.constants.FinancialAccountConstants;
import com.example.demo.service.subgraph.db.DbSubGraphService;
import com.example.demo.service.subgraph.db.FinancialDataService;
import com.example.demo.util.FinancialMetricsCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DartFinancialDataService implements FinancialDataService {

    private final DbSubGraphService dbSubGraphService;
    @Override
    public Map<String, Object> getFinancialData(String corpCode) throws IOException {
        // 재무정보 DB에서 가져오기
        List<Map<String, Object>> docs = dbSubGraphService.fetchStandardAccounts(corpCode);

        // dataset 만들기
        Map<String, Double> dataset = new HashMap<>();
        Map<String, Double> prevValues = new HashMap<>();
        prevValues.put("revenue", 0.0);
        prevValues.put("operating_income", 0.0);

        for (Map<String, Object> doc : docs) {
            String accountName = (String) doc.get("account_nm");
            Double thstrm = FinancialMetricsCalculatorUtils.toDouble(doc.get("thstrm_amount"));
            Double frmtrm = FinancialMetricsCalculatorUtils.toDouble(doc.get("frmtrm_amount"));

            String key = FinancialAccountConstants.KEY_MAP.get(accountName);
            if (key != null) {
                dataset.put(key, thstrm);
                if (prevValues.containsKey(key)) {
                    prevValues.put(key, frmtrm);
                }
            }
        }

        return FinancialMetricsCalculatorUtils.calculate(dataset, prevValues);
    }
}

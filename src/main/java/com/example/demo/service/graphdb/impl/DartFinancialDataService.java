package com.example.demo.service.graphdb.impl;

import com.example.demo.constants.DbSubGraphConstants;
import com.example.demo.service.graphdb.DbSubGraphService;
import com.example.demo.service.graphdb.FinancialDataService;
import com.example.demo.util.graphdb.FinancialUtils;
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
        List<Map<String, Object>> docs = dbSubGraphService.getStandardFinancialAccounts(corpCode);

        // dataset 만들기
        Map<String, Double> dataset = new HashMap<>();
        Map<String, Double> prevValues = new HashMap<>(Map.of(
                "revenue", 0.0,
                "operating_income", 0.0
        ));

        for (Map<String, Object> doc : docs) {
            String key = DbSubGraphConstants.KEY_MAP.get(doc.get("account_nm"));
            if (key == null) continue;

            dataset.put(key, FinancialUtils.toDouble(doc.get("thstrm_amount")));
            prevValues.computeIfPresent(key, (k, v) -> FinancialUtils.toDouble(doc.get("frmtrm_amount")));
        }

        return FinancialUtils.calculate(dataset, prevValues);
    }
}

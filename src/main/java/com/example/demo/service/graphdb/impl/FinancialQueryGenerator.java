package com.example.demo.service.graphdb.impl;

import com.example.demo.service.graphdb.QueryGenerator;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.Script;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("financialQueryGenerator")
public class FinancialQueryGenerator implements QueryGenerator {
    private static final double ABS_TOLERANCE = 0.9;

    @Override
    public Query generate(String corpCode, Map<String, Object> finData) {
        // Map<String, Object> -> Map<String, JsonData>
        Map<String, JsonData> params = new HashMap<>();
        for (Map.Entry<String, Object> entry : finData.entrySet()) {
            params.put(entry.getKey(), JsonData.of(entry.getValue()));
        }

        return Query.of(q -> q
                .scriptScore(ss -> ss
                        .query(inner -> inner
                                .bool(b -> b
                                        // 자기 자신 제외
                                        .mustNot(mn -> mn.term(t -> t
                                                .field("corp_code")
                                                .value(FieldValue.of(corpCode))
                                        ))
                                        .filter(f -> f.range(r -> r
                                                .field("financials.revenue")
                                                .gte(JsonData.of((Double) finData.get("revenue") * (1 - ABS_TOLERANCE)))
                                                .lte(JsonData.of((Double) finData.get("revenue") * (1 + ABS_TOLERANCE)))
                                        ))
                                        .filter(f -> f.range(r -> r
                                                .field("financials.total_assets")
                                                .gte(JsonData.of((Double) finData.get("total_assets") * (1 - ABS_TOLERANCE)))
                                                .lte(JsonData.of((Double) finData.get("total_assets") * (1 + ABS_TOLERANCE)))
                                        ))
                                        .filter(f -> f.range(r -> r
                                                .field("financials.equity")
                                                .gte(JsonData.of((Double) finData.get("equity") * (1 - ABS_TOLERANCE)))
                                                .lte(JsonData.of((Double) finData.get("equity") * (1 + ABS_TOLERANCE)))
                                        ))
                                )
                        )
                        .script(Script.of(sc -> sc.inline(in -> in
                                .lang("painless")
                                .source("""
                        double score = 0;
                        score += 1 / (1 + Math.abs(doc['financials.operating_margin'].value - params.operating_margin));
                        score += 1 / (1 + Math.abs(doc['financials.net_margin'].value - params.net_margin));
                        score += 1 / (1 + Math.abs(doc['financials.debt_to_equity'].value - params.debt_to_equity));
                        score += 1 / (1 + Math.abs(doc['financials.equity_ratio'].value - params.equity_ratio));
                        score += 1 / (1 + Math.abs(doc['financials.operating_cf_to_investing_cf'].value - params.operating_cf_to_investing_cf));
                        score += 1 / (1 + Math.abs(doc['financials.operating_cf_to_revenue'].value - params.operating_cf_to_revenue));
                        score += 1 / (1 + Math.abs(doc['financials.revenue_growth'].value - params.revenue_growth));
                        score += 1 / (1 + Math.abs(doc['financials.operating_income_growth'].value - params.operating_income_growth));
                        return score;
                    """)
                                .params(params)
                        )))
                )
        );
    }
}
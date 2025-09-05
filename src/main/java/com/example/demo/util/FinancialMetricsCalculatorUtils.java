package com.example.demo.util;

import java.util.HashMap;
import java.util.Map;

public class FinancialMetricsCalculatorUtils {
    public static Map<String, Object> calculate(Map<String, Double> dataset, Map<String, Double> prevValues) {
        double revenue = dataset.getOrDefault("revenue", 0.0);
        double operatingIncome = dataset.getOrDefault("operating_income", 0.0);
        double netIncome = dataset.getOrDefault("net_income", 0.0);
        double totalAssets = dataset.getOrDefault("total_assets", 0.0);
        double equity = dataset.getOrDefault("equity", 0.0);
        double cfo = dataset.getOrDefault("cash_flow_operating", 0.0);
        double cfi = dataset.getOrDefault("cash_flow_investing", 0.0);

        Map<String, Object> result = new HashMap<>(dataset);

        result.put("operating_margin", revenue != 0 ? operatingIncome / revenue : 0);
        result.put("net_margin", revenue != 0 ? netIncome / revenue : 0);
        result.put("debt_to_equity", equity != 0 ? (totalAssets - equity) / equity : 0);
        result.put("equity_ratio", totalAssets != 0 ? equity / totalAssets : 0);
        result.put("operating_cf_to_investing_cf", cfi != 0 ? cfo / cfi : 0);
        result.put("operating_cf_to_revenue", revenue != 0 ? cfo / revenue : 0);

        double prevRevenue = prevValues.getOrDefault("revenue", 0.0);
        double prevOperatingIncome = prevValues.getOrDefault("operating_income", 0.0);

        result.put("revenue_growth", prevRevenue != 0 ? (revenue - prevRevenue) / prevRevenue : 0);
        result.put("operating_income_growth", prevOperatingIncome != 0 ? (operatingIncome - prevOperatingIncome) / prevOperatingIncome : 0);

        return result;
    }


    public static Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();

        try {
            return Double.parseDouble(value.toString().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

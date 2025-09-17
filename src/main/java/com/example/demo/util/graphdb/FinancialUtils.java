package com.example.demo.util.graphdb;

import java.util.HashMap;
import java.util.Map;

public class FinancialUtils {
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


    public static Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number num) {
            return num.intValue(); // 5777.0 -> 5777
        }
        try {
            return (int) Double.parseDouble(value.toString().replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String convertToMarkdown(Map<String, Map<String, Double>> data) {
        StringBuilder sb = new StringBuilder();

        // 헤더 추가
        sb.append("|항목|당기|전기|전전기|\n");
        sb.append("|---|---|---|---|\n");

        // 각 row 추가
        for (Map.Entry<String, Map<String, Double>> entry : data.entrySet()) {
            String item = entry.getKey();
            Map<String, Double> values = entry.getValue();

            int thstrm = (int) Math.floor(values.getOrDefault("thstrm", 0.0) / 1_000_000);
            int frmtrm = (int) Math.floor(values.getOrDefault("frmtrm", 0.0) / 1_000_000);
            int bfefrmtrm = (int) Math.floor(values.getOrDefault("bfefrmtrm", 0.0) / 1_000_000);

            sb.append(String.format("|%s|%d|%d|%d|\n", item, thstrm, frmtrm, bfefrmtrm));
        }

        return sb.toString();
    }


}

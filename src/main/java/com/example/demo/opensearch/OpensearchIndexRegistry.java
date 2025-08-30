package com.example.demo.opensearch;

import java.util.LinkedHashMap;
import java.util.Map;

/** 인덱스명 -> 리소스 경로 */
public final class OpensearchIndexRegistry {
    private OpensearchIndexRegistry() {}

    public static Map<String, String> indexToResource() {
        Map<String, String> map = new LinkedHashMap<>();
        // 리포트류(공통 스키마)
        map.put("rpt_qt",    "opensearch/report_base.json");
        map.put("rpt_half",  "opensearch/report_base.json");
        map.put("rpt_biz",   "opensearch/report_base.json");
        map.put("rpt_sec_eq","opensearch/report_base.json");
        map.put("rpt_other", "opensearch/report_base.json");
        // 기업공시작성기준
        map.put("standard",  "opensearch/standard.json");
        // 투자위험요소 기재요령 안내서
        map.put("risk_standard",  "opensearch/risk_standard.json");
        return map;
    }
}
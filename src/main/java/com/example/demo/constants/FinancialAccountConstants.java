package com.example.demo.constants;

import java.util.List;
import java.util.Map;

public class FinancialAccountConstants {
    public static final List<String> STANDARD_ACCOUNTS = List.of(
            "매출액", "영업이익", "당기순이익", "자산총계", "자본총계",
            "영업활동현금흐름", "투자활동현금흐름", "재무활동현금흐름"
    );

    public static final Map<String, String> KEY_MAP = Map.of(
            "매출액", "revenue",
            "영업이익", "operating_income",
            "당기순이익", "net_income",
            "자산총계", "total_assets",
            "자본총계", "equity",
            "영업활동현금흐름", "cash_flow_operating",
            "투자활동현금흐름", "cash_flow_investing",
            "재무활동현금흐름", "cash_flow_financing"
    );
}

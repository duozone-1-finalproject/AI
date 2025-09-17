package com.example.demo.constants;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DbSubGraphConstants {
    // 재무정보 관련 상수
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
    
    // 파싱정보 관련 상수
    public static final Pattern TD_PATTERN = Pattern.compile(
            "<td>(.*?)</td>|<td><p>(.*?)</p></td>", Pattern.DOTALL
    );

    public static final Pattern ITEM_PATTERN = Pattern.compile(
            "^(가\\.|나\\.|다\\.|라\\.|마\\.|바\\.|사\\.|아\\.|자\\.|차\\.|카\\.|타\\.|파\\.|하\\.)\\s*(.*?)(?=(?:\\n(가\\.|나\\.|다\\.|라\\.|마\\.|바\\.|사\\.|아\\.|자\\.|차\\.|카\\.|타\\.|파\\.|하\\.)|$))",
            Pattern.DOTALL | Pattern.MULTILINE
    );
    
}

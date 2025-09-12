package com.example.demo.constants;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Map.entry;

public class MainGraphConstants {
    public static final Pattern VAR_PATTERN = Pattern.compile("<([A-Za-z_][A-Za-z0-9_]*)>");

    /** otherRiskInput: [{offerTerms, lockupSchedules, dilutionOverhang, governanceAndDividend, stabilization, peerVolatility}] */
    public static final List<Map<String, Object>> OTHER_RISK_INPUT = List.of(
            Map.ofEntries(
                    // offerTerms
                    entry("offerTerms", Map.ofEntries(
                            entry("offerShares", Map.ofEntries(
                                    entry("plannedTotal", 3_636_641),
                                    entry("finalTotal", 3_385_000),
                                    entry("unit", "주")
                            )),
                            entry("offeringStructure", Map.ofEntries(
                                    entry("newShares", 3_100_000),
                                    entry("oldShares", 536_641)
                            )),
                            entry("priceBand", Map.ofEntries(
                                    entry("min", 15_000),
                                    entry("max", 18_000),
                                    entry("currency", "KRW")
                            )),
                            entry("finalOfferPrice", Map.ofEntries(
                                    entry("price", 10_000),
                                    entry("currency", "KRW")
                            )),
                            entry("expectedMarketCapAtOffer", Map.ofEntries(
                                    entry("sharesOutstandingAtListing", 20_837_140),
                                    entry("offerPrice", 10_000),
                                    entry("marketCapKRW", 208_371_400_000L)
                            )),
                            entry("freeFloatPostIPO", Map.of("percent", 26.5))
                    )),

                    // lockupSchedules
                    entry("lockupSchedules", List.of(
                            Map.ofEntries(
                                    entry("holderType", "일부 보호예수 물량"),
                                    entry("unlockDate", "2023-09-26"),
                                    entry("unlockMethod", "일괄"),
                                    entry("unlockedShares", 803_750),
                                    entry("unlockedRatioPercent", 3.75)
                            ),
                            Map.ofEntries(
                                    entry("holderType", "대주주/특수관계인 등"),
                                    entry("unlockDate", "2025-03-26"),
                                    entry("unlockMethod", "일괄"),
                                    entry("unlockedShares", 4_984_650),
                                    entry("unlockedRatioPercent", 22.77)
                            )
                    )),

                    // dilutionOverhang
                    entry("dilutionOverhang", Map.ofEntries(
                            entry("stockOptions", Map.ofEntries(
                                    entry("unexercisedCount", 1_320_151),
                                    entry("immediatelyExercisable", 662_251),
                                    entry("exercisePriceRangeKRW", List.of(283, 7_920))
                            )),
                            entry("convertiblesWarrants", Map.ofEntries(
                                    entry("postIPO_CPS_issued", true),
                                    entry("issueDate", "2024-07-22")
                            )),
                            entry("exerciseWindow", "상장 직후 일부 SO 행사 가능")
                    )),

                    // governanceAndDividend
                    entry("governanceAndDividend", Map.ofEntries(
                            entry("ownershipConcentration", List.of(
                                    Map.ofEntries(
                                            entry("name", "이성현"),
                                            entry("percentPostIPO", 17.99)
                                    )
                            )),
                            entry("dividendPolicyHistory", "무배당(기록 없음)"),
                            entry("votingRights", Map.ofEntries(
                                    entry("type", "보통주"),
                                    entry("perShare", 1)
                            ))
                    )),

                    // stabilization
                    entry("stabilization", Map.ofEntries(
                            entry("greenshoe", Map.ofEntries(
                                    entry("present", false),
                                    entry("maxShares", 0)
                            )),
                            entry("stabilizationTradingPlan", "N/A")
                    )),

                    // peerVolatility
                    entry("peerVolatility", List.of(
                            Map.ofEntries(
                                    entry("peer", "KOSDAQ 반도체 지수"),
                                    entry("keywords", List.of("3~6M 변동성", "실적발표 변동", "수급 민감"))
                            ),
                            Map.ofEntries(
                                    entry("peer", "동종 IP/설계주"),
                                    entry("keywords", List.of("보호예수 이벤트", "AI/메모리 사이클"))
                            )
                    ))
            )
    );
}

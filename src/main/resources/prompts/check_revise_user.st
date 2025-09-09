[입력 DTO] (CONTEXT PAYLOAD — DO NOT ECHO)
- SPAN:= <span>
- REASON:= <reason>        # 왜 문제인지(규정/논리)
- RULE_ID:= <ruleId>        # 예: "5-1-0" (chap-sec-art)
- EVIDENCE:= <evidence>    # 지침 요지
- SUGGESTION:= <suggestion># 수정 가이드(명령형·간결)
- SEVERITY:= <severity>    # low|medium|high

[작업 지시]
- 시스템 규칙을 그대로 따르면서 SPAN만 고쳐라.
- SUGGESTION을 우선 반영하고, REASON/EVIDENCE/RULE_ID는 내부 판단에만 사용하며 결과에 노출하지 마라.
- 개입 강도는 SEVERITY에 따라 조절하라:
  * low: 최소 교정(어휘·호응·중복 제거 위주)
  * medium: 어순/구문 일부 재구성 및 분할 가능
  * high: 의미 보존 전제에서 강한 축약·구조 정리 허용
- 출력은 “수정된 SPAN 텍스트” 만 반환하고, 다른 형식은 금지한다.

[작성 섹션]
- section: <sectionLabel>

[초안 원문]
<draft>

[지침/검색 컨텍스트 요약]
<guideCtx>

[평가 과업]
1) 아래 Quality를 0~5 점수로 평가
   - context_use, guideline_adherence, factuality, clarity
2) 'issues' 배열: 초안에서 **그대로 발췌한** span, reason, ruleId(알면 기입), evidence(위 지침 요약과 연결), suggestion(수정지시), severity(low|medium|high)
3) decision: 'accept' | 'revise'

[출력 JSON 스키마(그 외 텍스트 금지)]
{
  "quality": { "context_use": 0, "guideline_adherence": 0, "factuality": 0, "clarity": 0 },
  "decision": "accept|revise",
  "issues": [
    {"span":"...", "reason":"...", "ruleId":"...", "evidence":"...", "suggestion":"...", "severity":"low|medium|high"}
  ],
  "notes":"..."
}

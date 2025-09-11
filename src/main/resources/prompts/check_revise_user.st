[INPUT DTO] (data payload — do not echo)
SPAN := <span>
REASON := <reason>
RULE_ID := <ruleId>
EVIDENCE := <evidence>
SUGGESTION := <suggestion>
SEVERITY := <severity>   # low | medium | high

[TASK]
- Respond in Korean only.
- Edit SPAN only; do not alter any content outside SPAN (including spaces, line breaks, punctuation).
- Use SUGGESTION as a reference, not verbatim. If it conflicts with numbers/units/dates/amounts/percentages/proper nouns in SPAN, keep SPAN’s values and adjust wording only.
- Apply intervention by SEVERITY (as defined in the system prompt).
- Length may increase as needed for accuracy and completeness within the severity limits (approx ±5% / ±15% / ±25% for low/medium/high).
- Output exactly one thing: the revised SPAN text only. No quotes, no metadata, no markdown, no JSON.

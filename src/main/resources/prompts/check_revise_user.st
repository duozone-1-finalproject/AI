[SINGLE SPAN REVISE — MINIMAL EDIT]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- SPAN       := <span>        // exactly one sentence from draft; includes end punctuation;
- REASON     := <reason>      // concise rationale for revision
- RULE_ID    := <ruleId>      // exact ID(TITLE) from guideCtx; "" if none
- EVIDENCE   := <evidence>    // key substring from the same guideCtx DETAIL; "" if none
- SUGGESTION := <suggestion>  // reference wording; do not copy verbatim
- SEVERITY   := <severity>    // low | medium | high

SCOPE
- Edit SPAN only; do not alter any content outside SPAN (including surrounding spaces/line breaks/punctuation).
- Use SUGGESTION as a reference only. If it conflicts with numbers/units/dates/amounts/percentages/proper nouns in SPAN, keep SPAN’s values and adjust wording.
- Apply intervention proportional to SEVERITY.

RULES (exactly these)
1) Preserve the original meaning and factual values in SPAN; do not invent new facts.
2) Keep SPAN as a single sentence with proper end punctuation; do not split into multiple sentences.
3) Improve clarity, grammar, specificity, and consistency according to SEVERITY:
   • low: minimal touch-ups; length change within approx ±5%.
   • medium: moderate rewriting; length change within approx ±15%.
   • high: substantial rewrite; length change within approx ±25%.
4) Maintain formal register; avoid colloquialisms and exaggerated/definitive wording.
5) Do not add headings, lists, tables, or new sections.

EDITING GUIDELINES
- Prefer precise rewording over deletion; remove redundancies only when necessary.
- Normalize spacing, punctuation, and number/unit formatting within the sentence.
- Preserve defined terms and terminology unless clearly erroneous.
- Do not quote or reference INPUTS in the output.

OUTPUT (strict)
- Return ONLY the revised SPAN sentence (Korean text). No quotes, no metadata, no markdown, no JSON.

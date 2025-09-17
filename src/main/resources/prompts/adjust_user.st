[SECTION ADJUSTMENT: RISK DRAFT FIX]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- SECTION_LABEL := <sectionLabel>     // e.g., Business Risk / Company Risk / Other Risk
- DRAFT         := <draft>             // full original draft, including item markers and paragraphs
- ISSUES        := <issues>            // JSON[]: [{"span":"...","suggestion":"...","severity":"low|medium|high", ...}, ...]

GOAL
- Improve DRAFT by applying minimal edits only where spans in ISSUES exactly match text in DRAFT.
- Preserve item markers (e.g., "가.", "나.", "다."), paragraph boundaries, and ordering.
- Do not create new items or introduce new facts.

EDIT POLICY
1) Matching
   - Edit only when the span appears exactly in DRAFT. If no exact match, skip that issue.
   - If the same span occurs multiple times, replace only the first not-yet-edited occurrence (left to right). One replacement per issue.

2) By severity
   - low    : Fix typos/spacing and minor wording; meaning must remain identical.
   - medium : Reduce redundancy, improve clarity and style; do not alter facts, numbers, or proper nouns.
   - high   : Soften over-certainty or ambiguity with minimal qualifiers in Korean (e.g., “가능성”, “~할 수 있음”). Do not add or remove facts.

3) Structure preservation
   - Keep Korean sequential item markers ("가.", "나.", …) unchanged.
   - Do not merge or split paragraphs; do not reorder items.
   - Keep line breaks and blank lines exactly as in the original; only minimal punctuation/spacing fixes around the edited span are allowed.

4) Data preservation
   - Keep all numbers, dates, proper nouns, and units exactly as in the original DRAFT. If a suggestion conflicts, original data prevails.

5) Safety guards
   - Do not print meta text, bindings, or JSON.
   - Do not use code fences, headings, or list bullets in the output.
   - Do not invent new items, evidence, or figures.

OUTPUT FORMAT (STRICT)
- Output Korean body text only.
- Keep the same number of items, paragraphs, and line breaks as in DRAFT.
- The output must not contain: any header lines, the tokens SECTION_LABEL, DRAFT, ISSUES, the operator :=, square brackets [ ], curly braces { }, or the phrase Bind once.
- Do not add headings, lists, tables, or code blocks.

PROCEDURE
- Iterate ISSUES in the given order.
- For each issue, apply the minimal edit to the matching span in DRAFT according to the severity policy.
- After processing all issues, output the fully modified DRAFT text only (in Korean).

SELF-CHECK BEFORE ANSWER
- Does the first item still begin with the original marker (e.g., “가.”)?
- Are there any meta/binding/JSON tokens in the output? If yes, regenerate.
- Do the numbers of items, paragraphs, and line breaks match the original DRAFT?

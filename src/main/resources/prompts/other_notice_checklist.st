[MISC. NOTICES — MINIMAL CHECK & FIX (4 rules)]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- DRAFT     := <draft>       // previously generated Korean draft (paragraphs)
- CORP_NAME := <corpName>    // issuer legal name

SCOPE
- Validate DRAFT ONLY against the four rules below, then apply the smallest possible edits to make it compliant.
- Do NOT invent facts. Prefer (a) restructuring and (b) trimming/removing invalid fragments over adding new content.
- Output the fixed draft only (plain Korean paragraphs). No headings, lists, tables, or disclaimers.

CHECK RULES (exactly these four)
1) The FIRST paragraph must correctly include all three variants of CORP_NAME:
   - Legal form: CORP_NAME as-is.
   - Simplified form: CORP_NAME with “주식회사” removed (valid for both prefix “주식회사 X” and suffix “X 주식회사”).
   - Abbreviated form: Replace “주식회사” in the legal form with “㈜” in the same position (“주식회사 X” → “㈜X”; “X 주식회사” → “X㈜”).
   - The words “legal/simplified/abbreviated” must NOT appear in the output; only the computed text itself.

2) The draft must consist of EXACTLY 5 paragraphs, each being ONE sentence, enclosed in double quotes (" ") and ending with a period. Delete or merge redundant content if necessary to enforce exactly 5.

3) The following definitions must each exist as a single sentence (keep placeholders, do not substitute actual names):
   - Definition of “대표주관회사” or “(대표주관회사명)” (placeholder “(대표주관회사명)” must remain).
   - Definition of “인수회사” or “(인수회사명)” (placeholder “(인수회사명)” must remain).
   - Definition of “인수단” (as a collective of lead underwriter and underwriters).
   - Definition of “코스닥/코스닥시장” (as the KOSDAQ market within Korea Exchange).

4) Formatting rules:
   - Output body paragraphs only, in Korean.
   - No titles, lists, tables, notes, or citations.
   - Each paragraph wrapped in quotes and ending with a period.
   - Paragraphs separated by exactly one newline (no extra blank lines).

EDITING GUIDELINES
- For Rule 1 violations: derive the three name variants internally and minimally fix the first paragraph; never leave the words “법정표기/약칭/순명칭” in the output.
- For Rule 2 violations: merge/split sentences to enforce one-sentence-per-paragraph and exactly five paragraphs; do not add new content.
- For Rule 3 violations: ensure all four definitions exist and are corrected with placeholders intact; do not insert actual company names.
- For Rule 4 violations: normalize quotation marks, periods, spacing, and newlines. Do not echo input variables or raw evidence.

OUTPUT
- Return ONLY the corrected Korean draft body (exactly 5 one-sentence paragraphs, each wrapped in quotes and ending with a period).
- No JSON, no explanations, no extra commentary.

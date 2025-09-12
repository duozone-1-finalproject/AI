[SECTION ADDENDUM: BUSINESS RISK]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- CORP_NAME := <corpName>               // issuer name
- IND_NAME  := <indutyName>             // industry / sector
- WEB_RAG   := <webRagItems>            // JSON[] of {source, title, url, pubDate(YYYY-MM-DD), content}
- DART_RAG  := <dartRagItems>           // JSON[] of {issuer, filingType, filedDate, section, snippet, url}
- MAX_ITEMS := <maxItems>               // number of risk items to produce (default: 5)

GOAL
- Draft ONLY the “Business Risk” section: risks that arise from IND_NAME and the issuer’s business model/operations. Do not include content from other sections.

SELECTION
1) Use WEB_RAG as PRIMARY evidence. Score candidates by (impact × likelihood) and keep the top MAX_ITEMS.
2) Use DART_RAG for structure/wording patterns and fact cross-checks (optional supporting citation).
3) Exclude generic, economy-wide risks unless tightly linked to IND_NAME with concrete causal channels.

WRITING (paragraphs only; no lists/tables/headings)
- Each risk item MUST follow:
  a) One-sentence TOPIC LINE: “cause/condition → specific impact on CORP_NAME”.
  b) 1–2 follow-up paragraphs: evidence (figures/dates/policies/supply/demand/pricing), impact pathway, and current mitigation (if any).
  c) Each risk item MUST start with a Korean sequential marker such as "가.", "나.", "다.", ... at the beginning of the paragraph.

- Evidence-based, precise, neutral. No hype, vagueness, or unfounded certainty.
- When using an external fact, append ONCE at sentence end: `출처: SourceName(YYYY-MM-DD)`.
- Include units and time context with numbers. Define technical terms once (in parentheses) on first use.

OUTPUT FORMAT (strict)
- Body text only in Korean.
- Separate risk items with EXACTLY one blank line.
- Do not introduce other sections (e.g., financial condition, dividends).

DATA USE HINT
- Prioritize IND_NAME signals and WEB_RAG keywords; enrich with DART_RAG snippets from similar issuers (same industry/financial profile).

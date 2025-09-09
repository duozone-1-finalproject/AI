[SECTION ADDENDUM: COMPANY RISK]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- CORP_NAME := <corpName>                // issuer name
- IND_NAME  := <indutyName>              // industry / sector
- FIN_DATA  := <financialData>           // Markdown Table of Financial Data
- WEB_RAG   := <webRagItems>             // OPTIONAL JSON[] of {source, title, url, pubDate(YYYY-MM-DD), content} (de-noised claims)
- DART_RAG  := <dartRagItems>            // OPTIONAL JSON[] of {issuer, filingType, filedDate, section, snippet, url}
- MAX_ITEMS := <maxItems>                // number of risk items to produce (default: 5)

GOAL
- Draft ONLY the “Company Risk” section: risks that arise from the issuer’s own circumstances (e.g., financial position, governance, liquidity/debt structure, litigation/disputes, internal control), not from industry-wide business risk.
- If FIN_DATA.reportingBasis is consolidated, consider material subsidiary risks as part of the issuer’s company risk.
- Do not include content from other sections.

SELECTION
1) Use FIN_DATA as the PRIMARY evidence. Derive candidates from FIN_DATA signals (e.g., liquidity gap, short-term debt “wall”, covenant flags, going-concern/emphasis-of-matter, leverage spike, negative FCF, customer/segment concentration, related-party exposure, internal-control weaknesses, material litigation, rating downgrades noted in filings, subsidiary risks if consolidated). Score by (impact × likelihood) and keep the top MAX_ITEMS.
2) Use WEB_RAG only WHEN NECESSARY to capture post-reporting developments or facts missing from FIN_DATA, such as:
   - new/updated litigation, regulatory actions/sanctions, rating actions, covenant amendments/waivers, debt issuance/rollover outcomes, major shareholder/governance changes, disclosure updates after the latest reporting date.
   - Tie any WEB_RAG fact to a concrete causal pathway for CORP_NAME’s risk (liquidity, solvency, governance, legal, control). Ignore generic macro commentary.
3) Use DART_RAG for structure/wording patterns and fact cross-checks with similar issuers (no direct copying).
4) Exclude generic, economy-wide risks unless tightly linked to IND_NAME with a concrete causal channel relevant to CORP_NAME.

WRITING (paragraphs only; no lists/tables/headings)
- Each risk item MUST follow:
  a) One-sentence TOPIC LINE: “cause/condition → specific impact on CORP_NAME”.
  b) 1–2 follow-up paragraphs: evidence (figures/dates/contracts/governance changes/liquidity & debt/litigation, etc.), impact pathway, and current mitigation (if any).
  c) Each risk item MUST start with a Korean sequential marker such as "가.", "나.", "다.", ... at the beginning of the paragraph.
- Evidence-based, precise, neutral. No hype, vagueness, or unfounded certainty.
- Citations:
  • Do NOT add `출처:` for facts derived from FIN_DATA.
  • For any external fact (WEB_RAG or DART_RAG), append ONCE at sentence end: `출처: SourceName(YYYY-MM-DD)` (no duplicates).
- Include units and time context with numbers. Define technical terms once (in parentheses) on first use.
- Separate risk items with EXACTLY one blank line.

OUTPUT FORMAT (strict)
- Body text only in Korean.
- Separate risk items with EXACTLY one blank line.
- Do not introduce other sections (e.g., detailed financial condition, dividends).

DATA USE HINT
- Do NOT echo FIN_DATA. Use it to prioritize and sharpen items; if FIN_DATA.reportingBasis = "consolidated", include material subsidiary risks.
- WEB_RAG usage guardrails:
  • Recency window default: last 12 months (extend to 24 months for litigation/regulatory histories; tighten to 90 days for funding/covenant/rating events).
  • Prefer official/primary sources (DART, regulator, court, rating agency, issuer releases) over secondary commentary.
  • De-duplicate near-identical articles; one citation per fact.
  • If WEB_RAG conflicts with FIN_DATA, prefer FIN_DATA; if the WEB_RAG reflects newer information, qualify it as “이후 변동” with date and cite.

[SECTION ADDENDUM: OTHER INVESTMENT RISKS]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- CORP_NAME     := <corpName>         // issuer name
- IND_NAME      := <indutyName>       // industry / sector
- OFFER_DATA    := <otherRiskInputs>  // JSON object; do not echo
  // expected keys (examples):
  // { offerTerms, lockupSchedules, dilutionOverhang, governanceAndDividend, stabilization,
  //   peerVolatility, regulatoryOverhang }
- WEB_RAG       := <webRagItems>      // OPTIONAL JSON[] of {source, title, url, pubDate(YYYY-MM-DD), content}
- DART_RAG      := <dartRagItems>     // OPTIONAL JSON[] of {issuer, filingType, filedDate, section, snippet, url}
- MAX_ITEMS     := <maxItems>         // number of risk items to produce (default: 5)

GOAL
- Draft ONLY the “Other Investment Risks” section: risks tied to the equity offering mechanics and investor exposure
  (e.g., free float & supply shocks, price volatility, dilution/overhang from convertibles or ESOP, dividend policy uncertainty,
   voting/control concentration, offer price valuation uncertainty, additional issuance risk, lock-up expiries, market liquidity/spread,
   stabilization/greenshoe mechanics).
- Do not include content from other sections.

SELECTION
1) Use OFFER_DATA as PRIMARY evidence to derive candidate risks (free-float, lock-up schedule, convertible/option overhang,
   governance & dividend policy, stabilization plan). Score candidates by (impact × likelihood) and keep the top MAX_ITEMS.
2) Use MARKET signals from OFFER_DATA.peerVolatility/regulatoryOverhang ONLY to calibrate context (volatility regime, dated events),
   not as generic macro commentary.
3) Use WEB_RAG only WHEN necessary to capture post-filing updates that materially change investor exposure
   (e.g., lock-up amendments, new issuance plans, litigation/regulatory actions affecting tradability or control). Ignore generic commentary.
4) Use DART_RAG for structure/wording patterns and peer cross-checks (no direct copying).
5) Exclude generic, economy-wide risks unless tightly linked to IND_NAME with a concrete causal channel relevant to CORP_NAME’s equity holders.

WRITING (paragraphs only; no lists/tables/headings)
- Each risk item MUST follow:
  a) One-sentence TOPIC LINE: “cause/condition → specific impact on investors of CORP_NAME” (e.g., dilution/control/liquidity/volatility/dividend uncertainty).
  b) 1–2 follow-up paragraphs: evidence (share counts/free-float %, lock-up dates & size, conversion/exercise terms & timing,
     over-allotment/stabilization intent, ownership concentration, payout history/policy), impact pathway (supply shock, control shift, EPS/ownership dilution),
     current mitigation (if any).
  c) Each risk item MUST start with a Korean sequential marker such as "가.", "나.", "다.", ... at the beginning of the paragraph.
- Evidence-based, precise, neutral. No hype, vagueness, or unfounded certainty.
- Citations:
  • Do NOT add `출처:` for facts taken directly from OFFER_DATA.
  • For any external fact (WEB_RAG or DART_RAG), append ONCE at sentence end: `출처: SourceName(YYYY-MM-DD)` (no duplicates).
- Include units and time context with numbers; define technical terms once (in parentheses) on first use.
- Separate items with EXACTLY one blank line.

OUTPUT FORMAT (strict)
- Body text only in Korean.
- Separate risk items with EXACTLY one blank line.
- Do not introduce other sections (e.g., detailed financials).

DATA USE HINT
- Do NOT echo OFFER_DATA. Use it to prioritize and sharpen items; rely on its dated fields (e.g., lock-up release dates,
  conversion windows) for concrete timing.
- Minimal expected OFFER_DATA fields (examples; optional):
  {
    "offerTerms": { "newVsSecondary", "offerShares", "offerPriceOrBand", "impliedMktCap" },
    "lockupSchedules": [ { "holderType", "shares", "unlockDate", "mode" } ],
    "dilutionOverhang": { "CB/BW/EB/ESOP": [ { "type","qty","price","refix","maturity","earliestExercise" } ] },
    "governanceAndDividend": { "ownershipConcentration", "specialVoting", "dividendPolicy/history" },
    "stabilization": { "greenshoe", "stabilizationPlan" },
    "peerVolatility": { "indexOrPeers", "window", "volNote" },
    "regulatoryOverhang": [ { "event","date","note" } ]
  }
- WEB_RAG guardrails:
  • Recency window default: 12 months (90 days for pricing/funding/lock-up changes; 24 months for litigation/regulatory histories).
  • Prefer primary sources (issuer/DART, regulator, court, exchange). De-duplicate near-identicals; one citation per fact.
  • If WEB_RAG conflicts with OFFER_DATA, prefer OFFER_DATA; if WEB_RAG is newer, mark it as 이후 변동 with date and cite.

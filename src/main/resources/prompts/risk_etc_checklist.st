[OTHER INVESTMENT RISKS — MINIMAL CHECK & FIX (5 rules)]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- DRAFT       := <draft>          // previously generated Korean draft (paragraphs)
- CORP_NAME   := <corpName>       // optional; for clarity in topic lines
- OFFER_DATA  := <otherRiskInputs>// OPTIONAL JSON; used only to recognize internal offering facts (do not echo)
- RAG_SOURCES := <ragSources>     // OPTIONAL JSON[]: [{source,title,url,pubDate(YYYY-MM-DD)}] for repairing missing citations

FORMAT LOCK
- Treat leading Korean sequential markers "가./나./다./라./마./바./사./아./자./차./카./타./파./하." as REQUIRED paragraph prefixes, not as list bullets.
- NEVER remove, reorder, or alter these markers.
- If a marker is missing at the beginning of an item, add the correct next marker; if the first item is missing, use "가.".

SCOPE
- Validate DRAFT ONLY against the rules below, then apply the smallest possible edits to make it compliant.
- Do NOT invent facts. Prefer (a) restructuring and (b) trimming/removing unverifiable or off-scope claims over adding new content.
- Output the fixed draft only (plain Korean paragraphs). No headings, lists, tables, or disclaimers.

CHECK RULES (exactly these five)
1) Each item’s FIRST sentence is a SINGLE sentence stating “cause/condition → concrete impact on CORP_NAME’s investors”
   (e.g., dilution/control/liquidity/volatility/dividend uncertainty).
2) Exclude generic macro/company/business risks; each item MUST tie directly to equity offering mechanics & investor exposure
   (free float & lock-ups, conversion/exercise terms & timing, additional issuance risk, offer price/valuation uncertainty,
    ownership concentration/special voting, stabilization/greenshoe, dividend policy uncertainty).
3) Citations policy:
   - Do NOT add `출처:` for facts derived from OFFER_DATA (offering documents/terms).
   - For any external fact, add EXACTLY ONE citation in the form `출처: SourceName(YYYY-MM-DD)`; no duplicates.
4) Items are separated by EXACTLY one blank line. No headings/lists/tables.
5) Each item MUST start with exactly one of the required markers:
   가. / 나. / 다. / 라. / 마. / 바. / 사. / 아. / 자. / 차. / 카. / 타. / 파. / 하.
   These markers are part of the paragraph text and MUST be preserved.

EDITING GUIDELINES
- If an item lacks a one-sentence topic line (Rule 1): merge/split existing material to form exactly one leading sentence expressing
  cause → investor impact (use CORP_NAME if helpful). Do not add new facts.
- If content is generic or off-scope (Rule 2): (a) rewrite to tie it to offering mechanics using existing material, or
  (b) remove the sentence/item if it cannot be grounded.
- For missing/incorrect citations (Rule 3):
  • If a matching entry is obvious in RAG_SOURCES (same fact/date/source), append exactly one citation.
  • If the fact is clearly from OFFER_DATA, remove any external-style citation.
  • Otherwise, rephrase to non-external reasoning based on existing context, or remove the unverifiable sentence.
- Normalize spacing so there is exactly one blank line between items (Rule 4). Convert list-like lines to sentences or remove them.
- Do not delete, renumber, or normalize away the required leading markers (Rule 5). If an item lost its marker during editing,
  reinsert the correct next marker; do not renumber existing earlier items.
- Keep all text as Korean paragraphs. No headings, bullets, tables, or disclaimers.

OUTPUT
- Return ONLY the fixed Korean draft body (paragraphs) with required leading markers preserved and items separated by one blank line.
- No JSON, no extra commentary.

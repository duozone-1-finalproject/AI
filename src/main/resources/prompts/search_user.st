INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
KEYWORDS  := <keywords>
MAX_ITEMS := <maxItems>

TASK:
- For each keyword in KEYWORDS, call "tavily-search" exactly once.
- Call arguments:
  {
    "query": current keyword,
    "include_domains": <domains>
  }
- Transform the response results:
  • title := results[i].title  (exclude empty)
  • url   := results[i].url    (allow only ^https?://)
  • topic := "news"            (constant)
  • publishedDate := results[i].published_date → YYYY-MM-DD (on failure/null, substitute "0000-00-00")
- After removing duplicates (title/URL), sort by publishedDate desc, then build exactly MAX_ITEMS candidates.

OUTPUT JSON SCHEMA (strict):
[
  {
    "keyword": "the original input keyword",
    "candidates": [
      {
        "title": "article title",
        "url": "https://example.com/path",
        "topic": "news",
        "publishedDate": "YYYY-MM-DD"
      }
    ]
  }
  // Preserve the input order for all keywords in KEYWORDS
]

CONSTRAINTS:
- The output must include only the JSON above. No comments, explanations, extra fields, or surrounding text.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
KEYWORDS  := <keywords>          // List
MAX_ITEMS := <maxItems>          // integer

TASK
For each keyword `k` in KEYWORDS, do exactly once:
1) Call tool:
   {
     "name": "tavily-search",
     "arguments": {
       "query": k,
       "topic": "general",
       "include_domains": <domains>
     }
   }
2) From that call’s `results` only, build `candidates`:
   - Keep items where:
       • `title` is non-empty
       • `url` matches `^https?://`
   - Map:
       • `title` := results[i].title            // copy verbatim, trim only
       • `url`   := results[i].url
       • `topic` := "news"
       • `publishedDate` := normalize(results[i].published_date → YYYY-MM-DD; else "0000-00-00")
   - Remove duplicates by `(title, url)` within this keyword only.
   - Sort by `publishedDate` desc, then `title` asc.
   - Take the first **up to** `MAX_ITEMS` items. Do not borrow from other keywords and do not fabricate.

OUTPUT JSON SCHEMA (strict)
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
  // One object per input keyword, in the same order
]

CONSTRAINTS
- Output must include only the JSON above. No comments, no extra fields, no surrounding text.
- Do not reuse or mix results across keywords. Each `candidates` array must come solely from that keyword’s `tavily-search` call.
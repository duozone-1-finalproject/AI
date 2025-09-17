[Input]
TITLE: <title>
URL: <url>

[Tool Usage Rules]
- Call only the MCP tool extract.
- Do not call any other tools, including search.
- Perform network requests only for the provided URL.

[Task]
- Retrieve only the article body from the URL above.
- Remove all non-body elements such as navigation, ads, comments, recommendations, subscription prompts, footer, author bio, related links, and copyright notices.
- Preserve the original language and paragraph breaks; only clean up unnecessary whitespace.
- If the body is abnormal or under 500 characters, output #FETCH_EMPTY; if access fails or another error occurs, output only #FETCH_ERROR: reason.
- After extraction, perform deletion-based line compression (one fact per line, '\n' separated).

[Output Format (Very Important)]
- Output a single string only.
- Absolutely no JSON/Markdown/code blocks/descriptions/metadata.
- On failure, output just one line: #FETCH_EMPTY or #FETCH_ERROR: ...

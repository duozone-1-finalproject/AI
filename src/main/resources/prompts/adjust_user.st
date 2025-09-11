[Bind once]
sectionLabel := <sectionLabel>
draft := <draft>
issues := <issues>   // JSON array like: [{"span":"...","suggestion":"...","severity":"low|medium|high", ...}, ...]

[Task]
- Read issues. For each item, rewrite only the matching span in the draft according to the severity policy, using suggestion as a reference.
- If a span has no exact match in the draft, skip it and continue with the next item.
- Produce only the final, fully modified draft text as the output (in Korean).

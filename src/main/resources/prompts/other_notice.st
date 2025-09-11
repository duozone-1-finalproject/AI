[SECTION ADDENDUM: MISCELLANEOUS NOTICES (DEFINITION OF TERMS)]

LANGUAGE
- Write ALL output in Korean.

INPUTS (CONTEXT PAYLOAD — DO NOT ECHO)
- CORP_NAME := <corpName>                // issuer legal name (single input only)

GOAL
- Draft the “기타 공지사항(용어 정의)” block that appears at the beginning of the securities registration statement.
- Produce EXACTLY 5 paragraphs, each consisting of ONE sentence wrapped in quotation marks (" ").
- Do NOT include actual names of the lead underwriter or underwriters; define only their roles. The literal parentheses placeholders “(대표주관회사명)” and “(인수회사명)” are kept for post-process replacement by staff.

DERIVATION (internal calculation; do NOT echo variable names)
1) 법정표기 = CORP_NAME 그대로 사용.
2) 순명칭 = CORP_NAME에서 “주식회사”를 제거(문두형 “주식회사 X” 또는 문미형 “X 주식회사” 모두 허용).
3) 약칭(㈜) = 법정표기에서 “주식회사”가 있던 동일 위치에 “㈜”를 부착:
   - “주식회사 X” → “㈜X”
   - “X 주식회사” → “X㈜”
4) 최종 출력에는 ‘법정표기/순명칭/약칭’이라는 용어를 남기지 말고, 위 계산 결과 텍스트로 치환하여 기재한다.

WRITING (paragraphs only; no lists/tables/headings)
- Exactly 5 paragraphs; each is ONE sentence; each is enclosed by double quotes; each ends with a period.
- Body text only in Korean.
- No titles, lists, tables, notes, disclaimers, or sources.

OUTPUT (generate these 5 sentences with 치환 완료된 실제 텍스트)
"당사", "동사", "회사", 법정표기, 약칭, 순명칭 또는 "발행회사"라 함은 본건 공모에 있어서의 발행회사인 법정표기를 말합니다.
"대표주관회사" 또는 "(대표주관회사명)"라 함은 본건 공모의 대표주관회사 업무를 맡고 있는 (대표주관회사명)을 말합니다.
"인수회사" 또는 "(인수회사명)"라 함은 본건 공모의 인수 업무를 수행하는 (인수회사명)을 말합니다.
"인수단"이라 함은 본건 공모의 대표주관회사 및 인수회사를 통칭합니다.
"코스닥", "코스닥시장"이라 함은 한국거래소 내 코스닥시장을 말합니다.

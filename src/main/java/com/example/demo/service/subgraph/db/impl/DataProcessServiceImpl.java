package com.example.demo.service.subgraph.db.impl;

import com.example.demo.dto.dbsubgraph.DbDocDto;
import com.example.demo.service.subgraph.db.DataProcessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataProcessServiceImpl implements DataProcessService {
    @Override
    public List<DbDocDto> processData(List<String> rawDocs) {

        List<DbDocDto> results = new ArrayList<>();

        // 정규식 패턴 준비
        Pattern tdPattern = Pattern.compile("<td>(.*?)</td>|<td><p>(.*?)</p></td>", Pattern.DOTALL);
        Pattern itemPattern = Pattern.compile(
                "^(가\\.|나\\.|다\\.|라\\.|마\\.|바\\.|사\\.|아\\.|자\\.|차\\.|카\\.|타\\.|파\\.|하\\.)\\s*(.*?)(?=(?:\\n(가\\.|나\\.|다\\.|라\\.|마\\.|바\\.|사\\.|아\\.|자\\.|차\\.|카\\.|타\\.|파\\.|하\\.)|$))",
                Pattern.DOTALL | Pattern.MULTILINE
        );

        for (int i = 0; i < rawDocs.size(); i++) {
            String raw = rawDocs.get(i);

            // <p>, </p> 제거
            String cleaned = raw.replaceAll("</?p>", "");

            // <td> 블록 추출
            Matcher tdMatcher = tdPattern.matcher(cleaned);
            List<String> tdTexts = new ArrayList<>();
            while (tdMatcher.find()) {
                String block = tdMatcher.group(1) != null ? tdMatcher.group(1) : tdMatcher.group(2);
                tdTexts.add(block);
            }

            // 항목 텍스트 추출 (리스트 그대로 저장)
            List<String> items = new ArrayList<>();
            for (String td : tdTexts) {
                Matcher itemMatcher = itemPattern.matcher(td);
                while (itemMatcher.find()) {
                    items.add(itemMatcher.group(1) + " " + itemMatcher.group(2).trim());
                }
            }

            // DTO 생성
            DbDocDto dto = new DbDocDto();
            dto.setId(String.valueOf(i));
            dto.setSec_content(items); // 🔹 join 안 하고 리스트 그대로 저장

            results.add(dto);
        }

        return results;
    }
}

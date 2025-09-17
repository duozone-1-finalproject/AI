package com.example.demo.util.graphdb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.example.demo.constants.DbSubGraphConstants.ITEM_PATTERN;
import static com.example.demo.constants.DbSubGraphConstants.TD_PATTERN;

public class ParsingUtils {
    public static List<String> extractItems(String rawHtml) {
        // <p> 태그 제거
        String cleaned = rawHtml.replaceAll("</?p>", "");

        // <td> 블록 추출
        Matcher tdMatcher = TD_PATTERN.matcher(cleaned);
        List<String> tdTexts = new ArrayList<>();
        while (tdMatcher.find()) {
            tdTexts.add(tdMatcher.group(1) != null ? tdMatcher.group(1) : tdMatcher.group(2));
        }

        // 항목 텍스트 추출
        List<String> items = new ArrayList<>();
        for (String td : tdTexts) {
            Matcher itemMatcher = ITEM_PATTERN.matcher(td);
            while (itemMatcher.find()) {
                items.add(itemMatcher.group(1) + " " + itemMatcher.group(2).trim());
            }
        }
        return items;
    }
}

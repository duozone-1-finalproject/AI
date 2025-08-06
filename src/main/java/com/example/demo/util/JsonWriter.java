package com.example.demo.util;

import com.example.demo.dto.SerpApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonWriter {

    public static void writeArticlesToFile(List<SerpApiResult> articles, String query) {
        System.out.println("✅ writeArticlesToFile 메서드 진입 성공");

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String safeQuery = query.replaceAll("[^가-힣a-zA-Z0-9]", "");
        String fileName = "articles/articles_" + safeQuery + ".json";

        Map<String, Object> data = new HashMap<>();
        data.put("articles", articles);

        try {
            File dir = new File("articles");
            if (!dir.exists()) {
                dir.mkdirs(); // 폴더 없으면 생성
            }

            System.out.println("✅ 기사 개수: " + articles.size());
            System.out.println("✅ 저장 경로: " + new File("articles").getAbsolutePath());

            // 🔽 이 아래에서 파일을 저장해야 함
            mapper.writeValue(new File(fileName), data);
            System.out.println("✅ JSON 저장 완료");
        } catch (IOException e) {
            System.out.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



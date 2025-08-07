//검색 결과를 JSON 파일로 저장
//
package com.example.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonWriter {

    private static final Logger log = LoggerFactory.getLogger(JsonWriter.class);

    private JsonWriter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void writeDataToFile(List<Map<String, String>> dataList, String query) throws IOException {
        log.info("JSON 파일 저장 프로세스를 시작합니다.");

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 파일명으로 사용할 수 없는 문자를 제거하고 길이를 제한합니다.
        String safeQuery = query.replaceAll("[^a-zA-Z0-9가-힣]", "_").replaceAll("__+", "_");
        if (safeQuery.length() > 50) {
            safeQuery = safeQuery.substring(0, 50);
        }

        // 애플리케이션 실행 위치를 기준으로 'output/json' 폴더에 저장합니다.
        String directoryPath = Paths.get("output", "json").toString();
        File dir = new File(directoryPath);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("출력 디렉토리 생성에 실패했습니다: " + dir.getAbsolutePath());
        }

        // 동일한 쿼리에 대해 파일이 덮어써지는 것을 방지하기 위해 타임스탬프 추가
        String fileName = "articles_" + safeQuery + "_" + System.currentTimeMillis() + ".json";
        File file = new File(dir, fileName);

        Map<String, Object> data = new HashMap<>();
        data.put("query", query);
        data.put("article_count", dataList.size());
        data.put("articles", dataList);

        log.info("총 {}개의 기사를 다음 경로에 저장합니다: {}", dataList.size(), file.getAbsolutePath());

        mapper.writeValue(file, data);
        log.info("JSON 파일 저장을 완료했습니다.");
    }
}

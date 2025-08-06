package com.example.demo.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWriterUtil {

    // 파일 저장 경로는 프로젝트 루트 디렉토리 하위에 "news_output.txt"
    private static final String FILE_PATH = "news_output.txt";

    // 기존 파일을 덮어쓰지 않고 계속 추가됨 (append=true)
    public static void writeToFile(String text) {
        try {
            Files.createFile(Paths.get(FILE_PATH)); // 파일 없으면 생성
        } catch (IOException ignored) {}

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(text);
            writer.newLine();
            writer.newLine();
        } catch (IOException e) {
            System.err.println("❌ 파일 저장 실패: " + e.getMessage());
        }
    }
}


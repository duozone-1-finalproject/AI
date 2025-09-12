package com.example.demo;

public class NewsCrawlerTest {
    public static void main(String[] args) {
        NewsCrawler crawler = new NewsCrawler();
        String testUrl = "https://news.nate.com/view/20250730n18454";
        String result = crawler.extractBody(testUrl);
        System.out.println("📄 추출된 본문:\n" + result);
    }
}


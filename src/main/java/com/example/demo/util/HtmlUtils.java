package com.example.demo.util;

public class HtmlUtils {
    public static String strip(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", " ");
    }
}
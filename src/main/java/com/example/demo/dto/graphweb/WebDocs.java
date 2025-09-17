package com.example.demo.dto.graphweb;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WebDocs implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
    private String title;
    private String url;
    private String source;
    private String date;
    private String content;

}

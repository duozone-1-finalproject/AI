package com.example.demo.dto.dbsubgraph;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DbDocDto implements Serializable {
    private String id;
    private List<String> sec_content;
}

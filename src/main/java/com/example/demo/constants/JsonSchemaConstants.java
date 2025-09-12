package com.example.demo.constants;

public class JsonSchemaConstants {

    // Validation JSON Schema (엄격/간결)
    public static final String VALIDATION_JSON_SCHEMA = """
            {
              "type":"object",
              "additionalProperties": false,
              "properties":{
                "guide":{"type":"string"},
                "quality":{
                  "type":"object",
                  "additionalProperties": false,
                  "properties":{
                    "guideline_adherence":{"type":"integer","minimum":0,"maximum":5},
                    "factuality":{"type":"integer","minimum":0,"maximum":5},
                    "clarity":{"type":"integer","minimum":0,"maximum":5}
                  },
                  "required":["guideline_adherence","factuality","clarity"]
                },
                "decision":{"type":"string","enum":["accept","revise"]},
                "issues":{
                  "type":"array",
                  "maxItems": 8,
                  "items":{
                    "type":"object",
                    "additionalProperties": false,
                    "properties":{
                      "span":{"type":"string","maxLength":180},
                      "reason":{"type":"string"},
                      "ruleId":{"type":"string"},
                      "evidence":{"type":"string","maxLength":300},
                      "suggestion":{"type":"string"},
                      "severity":{"type":"string","enum":["low","medium","high"]}
                    },
                    "required":["span","reason","ruleId","evidence","suggestion","severity"]
                  }
                },
                "notes":{"type":"string","maxLength":200}
              },
              "required":["guide","quality","decision","issues","notes"]
            }
            """;
}

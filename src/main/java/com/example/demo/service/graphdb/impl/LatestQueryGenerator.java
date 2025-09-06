package com.example.demo.service.graphdb.impl;

import com.example.demo.service.graphdb.QueryGenerator;
import org.opensearch.client.opensearch._types.Script;
import org.opensearch.client.opensearch._types.query_dsl.FunctionBoostMode;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("latestQueryGenerator")
public class LatestQueryGenerator implements QueryGenerator {

    @Override
    public Query generate(String corpCode, Map<String, Object> params) {
        return Query.of(q -> q
                .functionScore(fs -> fs
                        .query(inner -> inner.matchAll(m -> m))
                        .functions(f -> f
                                .scriptScore(sc -> sc
                                        .script(Script.of(s -> s.inline(i -> i
                                                .lang("painless")
                                                .source("doc['pub_date'].value.millis") // 최신순 점수
                                        )))
                                )
                        )
                        .boostMode(FunctionBoostMode.Replace)
                )
        );
    }
}

package com.example.demo.service.graphdb.impl;

import com.example.demo.service.graphdb.QueryGenerator;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("industryQueryGenerator")
public class IndustryQueryGenerator implements QueryGenerator {

    @Override
    public Query generate(String corpCode, Map<String, Object> params) {
        // industry_code 앞 2자리 추출
        String indCode = params.get("ind_code").toString();
        String industryPrefix = indCode.substring(0, 2);

        return Query.of(q -> q
                .bool(b -> b
                        // 자기 자신 제외
                        .mustNot(mn -> mn.term(t -> t
                                .field("corp_code")
                                .value(FieldValue.of(corpCode))
                        ))
                        // industry_code 가 해당 prefix 로 시작하는 것 검색
                        .must(m -> m.prefix(p -> p
                                .field("induty_code")
                                .value(industryPrefix)
                        ))
                )
        );
    }
}

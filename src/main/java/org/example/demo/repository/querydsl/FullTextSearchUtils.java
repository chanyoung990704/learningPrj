package org.example.demo.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.util.StringUtils;

public class FullTextSearchUtils {

    /**
     * MySQL의 MATCH AGAINST 구문을 위한 BooleanExpression을 생성합니다. (Natural Language Mode)
     * @param field 검색할 컬럼 (예: QPost.post.title)
     * @param keyword 검색어
     * @return BooleanExpression
     */
    public static BooleanExpression matchAgainst(StringPath field, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건을 추가하지 않음
        }
        // MATCH(column) AGAINST(keyword) > 0 형태로 생성
        // Natural Language Mode에서는 relevance score를 반환하므로, 0보다 큰 경우를 찾습니다.
        return Expressions.booleanTemplate("MATCH({0}) AGAINST({1}) > 0", field, keyword);
    }

    /**
     * MySQL의 MATCH AGAINST 구문을 위한 BooleanExpression을 생성합니다. (Boolean Mode)
     * @param field 검색할 컬럼 (예: QPost.post.title)
     * @param keyword 검색어 (Boolean Mode 연산자 포함 가능: +단어 -단어 *단어 등)
     * @return BooleanExpression
     */
    public static BooleanExpression matchAgainstBooleanMode(StringPath field, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건을 추가하지 않음
        }
        // MATCH(column) AGAINST(keyword IN BOOLEAN MODE) 형태로 생성
        return Expressions.booleanTemplate("MATCH({0}) AGAINST({1} IN BOOLEAN MODE)", field, keyword);
    }
}
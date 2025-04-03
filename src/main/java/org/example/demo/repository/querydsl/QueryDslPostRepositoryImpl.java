package org.example.demo.repository.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.example.demo.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class QueryDslPostRepositoryImpl implements QueryDslPostRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> findPostsBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO) {
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .leftJoin(post.category).fetchJoin()
                .leftJoin(post.user).fetchJoin()
                .where(createSearchCondition(requestDTO))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 조건 추가
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<? extends Post> pathBuilder = new PathBuilderFactory().create(post.getType());
            ComparablePath<Comparable> expression = pathBuilder.getComparable(order.getProperty(), Comparable.class);
            query.orderBy(new OrderSpecifier<Comparable>(order.isAscending() ? Order.ASC : Order.DESC, expression));
        }

        List<Post> posts = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(createSearchCondition(requestDTO));

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchOne);
    }

    private BooleanExpression createSearchCondition(PostSearchRequestDTO requestDTO) {
        if (!StringUtils.hasText(requestDTO.getSearchText())) {
            return null;  // 검색어가 없으면 모든 게시글 반환
        }

        String searchType = requestDTO.getSearchType();
        String searchText = requestDTO.getSearchText().trim();

        if ("title".equals(searchType)) {
            return post.title.containsIgnoreCase(searchText);
        } else if ("author".equals(searchType)) {
            return post.user.name.containsIgnoreCase(searchText);
        } else if ("category".equals(searchType)) {
            return post.category.name.containsIgnoreCase(searchText);
        }

        return null;  // searchType이 잘못된 경우 모든 게시글 반환
    }
}

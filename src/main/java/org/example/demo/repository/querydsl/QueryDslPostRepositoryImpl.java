package org.example.demo.repository.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.demo.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class QueryDslPostRepositoryImpl implements QueryDslPostRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager em;

    public Page<Post> findPostsBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO) {
        String searchText = requestDTO.getSearchText();
        if (searchText == null || searchText.trim().isEmpty()) {
            // 투 쿼리 방식: 1차로 id만 조회
            List<Long> postIds = queryFactory
                    .select(post.id)
                    .from(post)
                    .orderBy(post.updatedAt.desc(), post.id.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
            if (postIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            // 2차로 id in (...)으로 연관 엔티티까지 fetch join
            List<Post> posts = queryFactory
                    .selectFrom(post)
                    .leftJoin(post.user).fetchJoin()
                    .leftJoin(post.category).fetchJoin()
                    .where(post.id.in(postIds))
                    .orderBy(post.updatedAt.desc(), post.id.desc())
                    .fetch();
            long total = queryFactory.select(post.count()).from(post).fetchOne();
            return new PageImpl<>(posts, pageable, total);
        }

        // 네이티브 쿼리로 풀텍스트 검색
        String sql = "SELECT * FROM posts WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        List<Post> posts = em.createNativeQuery(sql, Post.class)
                .setParameter(1, searchText)
                .setParameter(2, pageable.getPageSize())
                .setParameter(3, pageable.getOffset())
                .getResultList();

        if (!posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).collect(java.util.stream.Collectors.toList());

            queryFactory
                    .selectFrom(post)
                    .leftJoin(post.user).fetchJoin()
                    .leftJoin(post.category).fetchJoin()
                    .where(post.id.in(postIds))
                    .fetch();
        }

        // count 쿼리
        String countSql = "SELECT COUNT(*) FROM posts WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)";
        Number total = (Number) em.createNativeQuery(countSql)
                .setParameter(1, searchText)
                .getSingleResult();

        return new PageImpl<>(posts, pageable, total.longValue());
    }

    public Page<PostListResponseDTO> findPostsBySearchWithUserAndCategoryV2(Pageable pageable, PostSearchRequestDTO requestDTO) {
        JPAQuery<PostListResponseDTO> query = queryFactory
                .select(Projections.constructor(PostListResponseDTO.class,
                        post.id,
                        post.title,
                        post.user.name,
                        post.updatedAt,
                        post.category))
                .from(post)
                .leftJoin(post.category)
                .leftJoin(post.user)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 검색어 조건 추가
        String searchText = requestDTO.getSearchText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            query.where(post.title.contains(searchText));
        }

        // 정렬 조건 추가
        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<? extends Post> pathBuilder = new PathBuilderFactory().create(post.getType());
            ComparablePath<Comparable> expression = pathBuilder.getComparable(order.getProperty(), Comparable.class);
            query.orderBy(new OrderSpecifier<Comparable>(order.isAscending() ? Order.ASC : Order.DESC, expression));
        }

        List<PostListResponseDTO> posts = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post);

        // 검색어 조건 추가 (count 쿼리에도 동일하게 적용)
        if (searchText != null && !searchText.trim().isEmpty()) {
            countQuery.where(post.title.contains(searchText));
        }

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<PostListResponseDTO> findPostsListBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO) {
        String searchText = requestDTO.getSearchText();
        if (searchText == null || searchText.trim().isEmpty()) {
            List<PostListResponseDTO> posts = queryFactory
                    .select(Projections.constructor(PostListResponseDTO.class,
                            post.id,
                            post.title,
                            post.user.name,
                            post.updatedAt,
                            post.category.name,
                            post.viewCount
                    ))
                    .from(post)
                    .leftJoin(post.user)
                    .leftJoin(post.category)
                    .orderBy(post.updatedAt.desc(), post.id.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            long total = queryFactory.select(post.count()).from(post).fetchOne();

            return new PageImpl<>(posts, pageable, total);
        }
        // 수정된 부분: view_count 컬럼 추가
        String sql = "SELECT p.id, p.title, u.name AS author, p.updated_at AS time, c.name AS category_name, p.view_count AS viewCount " +
                "FROM posts p " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN post_categories c ON c.id = p.post_category_id " +
                "WHERE MATCH(p.title) AGAINST (? IN BOOLEAN MODE) " +
                "ORDER BY p.updated_at DESC, p.id DESC LIMIT ? OFFSET ?";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, searchText)
                .setParameter(2, pageable.getPageSize())
                .setParameter(3, pageable.getOffset())
                .getResultList();

        List<PostListResponseDTO> posts = rows.stream()
                .map(row -> PostListResponseDTO.builder()
                        .id(row[0] == null ? null : ((Number) row[0]).longValue())
                        .title((String) row[1])
                        .author((String) row[2])
                        .time(row[3] == null ? null : ((Timestamp) row[3]).toLocalDateTime())
                        .categoryName((String) row[4])
                        .viewCount(row[5] == null ? 0L : ((Number) row[5]).longValue()) // viewCount 추가
                        .build()
                )
                .collect(Collectors.toList());

        String countSql = "SELECT COUNT(*) FROM posts WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)";
        Number total = (Number) em.createNativeQuery(countSql)
                .setParameter(1, searchText)
                .getSingleResult();

        return new PageImpl<>(posts, pageable, total.longValue());
    }

    @Override
    public Page<PostListResponseDTO> findPostsListBySearchWithUserAndCategoryV2(Pageable pageable, PostSearchRequestDTO requestDTO) {
        String searchText = requestDTO.getSearchText();
        if (searchText == null || searchText.trim().isEmpty()) {
            List<PostListResponseDTO> posts = queryFactory
                    .select(Projections.constructor(PostListResponseDTO.class,
                            post.id,
                            post.title,
                            post.user.name,
                            post.updatedAt,
                            post.category.name,
                            post.viewCount
                    ))
                    .from(post)
                    .leftJoin(post.user)
                    .leftJoin(post.category)
                    .where(post.category.id.eq(requestDTO.getCategoryId()))
                    .orderBy(post.updatedAt.desc(), post.id.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            long total = queryFactory.select(post.count())
                    .from(post)
                    .where(post.category.id.eq(requestDTO.getCategoryId()))  // 카테고리 조건 추가
                    .fetchOne();

            return new PageImpl<>(posts, pageable, total);
        }

        String sql = "SELECT p.id, p.title, u.name AS author, p.updated_at AS time, c.name AS category_name, p.view_count AS viewCount " +
                "FROM posts p " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN post_categories c ON c.id = p.post_category_id " +
                "WHERE MATCH(p.title) AGAINST (? IN BOOLEAN MODE) " +
                "AND c.id = ? " +
                "ORDER BY p.updated_at DESC, p.id DESC LIMIT ? OFFSET ?";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, searchText)
                .setParameter(2, requestDTO.getCategoryId())
                .setParameter(3, pageable.getPageSize())
                .setParameter(4, pageable.getOffset())
                .getResultList();

        List<PostListResponseDTO> posts = rows.stream()
                .map(row -> PostListResponseDTO.builder()
                        .id(row[0] == null ? null : ((Number) row[0]).longValue())
                        .title((String) row[1])
                        .author((String) row[2])
                        .time(row[3] == null ? null : ((Timestamp) row[3]).toLocalDateTime())
                        .categoryName((String) row[4])
                        .viewCount(row[5] == null ? 0L : ((Number) row[5]).longValue())
                        .build())
                .collect(Collectors.toList());

        String countSql = "SELECT COUNT(*) FROM posts p " +
                "JOIN post_categories c ON c.id = p.post_category_id " +
                "WHERE MATCH(p.title) AGAINST (? IN BOOLEAN MODE) AND c.id = ?";
        Number total = (Number) em.createNativeQuery(countSql)
                .setParameter(1, searchText)
                .setParameter(2, requestDTO.getCategoryId())
                .getSingleResult();

        return new PageImpl<>(posts, pageable, total.longValue());
    }

}

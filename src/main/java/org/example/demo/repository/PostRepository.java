package org.example.demo.repository;

import org.example.demo.domain.Post;
import org.example.demo.repository.querydsl.QueryDslPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, QueryDslPostRepository {

    // 기본 페이지 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 10;

    // --- 다중 게시글 조회 (목록/페이지) ---

    // 모든 게시글 조회 (작성자 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user")
    List<Post> findPostsWithUser();

    // 모든 게시글 조회 (작성자 및 카테고리 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    List<Post> findPostsWithUserAndCategory();

    // 모든 게시글 페이징 조회 (작성자 및 카테고리 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    Page<Post> findPostsWithUserAndCategory(Pageable pageable);


    // --- 단일 게시글 조회 (ID 기반) ---

    // ID로 특정 게시글 조회 (작성자 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findPostWithUser(@Param("id") Long id);

    // ID로 특정 게시글 조회 (작성자 및 카테고리 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategory(@Param("id") Long id);

    // ID로 특정 게시글 조회 (작성자, 카테고리 및 파일 정보 Fetch Join)
    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c LEFT JOIN FETCH p.files f WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategoryAndFiles(@Param("id") Long id);


    // --- 데이터 수정 ---

    // 게시글 조회수 1 증가
    @Modifying(clearAutomatically = true) // 영속성 컨텍스트 클리어 후 실행
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int updateViewCount(@Param("id") Long id);
}

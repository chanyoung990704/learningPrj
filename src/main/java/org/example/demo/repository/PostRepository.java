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

    int DEFAULT_PAGE_SIZE = 10;

    @Query("SELECT p FROM Post p JOIN FETCH p.user")
    List<Post> findPostsWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    List<Post> findPostsWithUserAndCategory();

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    Page<Post> findPostsWithUserAndCategory(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findPostWithUser(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategory(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c LEFT JOIN FETCH p.files f WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategoryAndFiles(@Param("id") Long id);

    @Query("SELECT c.name FROM Post p JOIN p.category c WHERE p.id = :id")
    Optional<String> findCategoryNameById(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int updateViewCount(@Param("id") Long id);
}
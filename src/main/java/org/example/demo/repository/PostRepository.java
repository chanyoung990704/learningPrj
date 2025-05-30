package org.example.demo.repository;

import org.example.demo.domain.Post;
import org.example.demo.repository.querydsl.QueryDslPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, QueryDslPostRepository {

    @Query("SELECT p FROM Post p JOIN FETCH p.user")
    List<Post> findPostsWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findPostWithUser(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    List<Post> findPostsWithUserAndCategory();

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    Page<Post> findPostsWithUserAndCategory(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategory(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c LEFT JOIN FETCH p.files f WHERE p.id = :id")
    Optional<Post> findByIdWithUserAndCategoryAndFiles(@Param("id") Long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.files f WHERE p.id = :id")
    Optional<Post> findByIdWithFiles(@Param("id") Long id);

    public static final int DEFAULT_PAGE_SIZE = 10;
}
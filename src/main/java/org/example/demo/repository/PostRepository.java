package org.example.demo.repository;

import org.example.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user")
    List<Post> findPostsWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findPostWithUser(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.category c")
    List<Post> findPostsWithUserAndCategory();
}
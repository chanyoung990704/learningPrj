package org.example.demo.repository;

import org.example.demo.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.post p " +
            "WHERE c.post.id = :id")
    List<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.id = :id")
    Optional<Comment> findCommentByIdWithUser(@Param("id") Long commentId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.post p " +
            "WHERE c.post.id = :id ORDER BY c.createdAt DESC, c.id DESC")
    Page<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id, Pageable pageable);


}
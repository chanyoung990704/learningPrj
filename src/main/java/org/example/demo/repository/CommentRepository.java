package org.example.demo.repository;

import org.example.demo.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.post p " +
            "WHERE c.post.id = :id")
    List<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id);
}
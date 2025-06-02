package org.example.demo.repository;

import org.example.demo.domain.Comment;
import org.example.demo.domain.CommentLike;
import org.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  // 특정 댓글의 좋아요 수 조회
  long countByComment(Comment comment);

  // 사용자가 특정 댓글에 좋아요를 눌렀는지 확인
  boolean existsByCommentAndUser(Comment comment, User user);

  // 댓글과 사용자로 좋아요 조회
  Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

  // 댓글 ID와 사용자 ID로 좋아요 조회 (JOIN FETCH 사용)
  @Query("SELECT cl FROM CommentLike cl JOIN FETCH cl.comment c JOIN FETCH cl.user u " +
          "WHERE c.id = :commentId AND u.id = :userId")
  Optional<CommentLike> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

  // 댓글 ID로 좋아요 삭제
  void deleteByComment(Comment comment);
}
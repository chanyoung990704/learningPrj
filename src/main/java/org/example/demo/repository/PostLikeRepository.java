package org.example.demo.repository;

import org.example.demo.domain.Post;
import org.example.demo.domain.PostLike;
import org.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 게시글의 좋아요 수 조회
    long countByPost(Post post);

    // 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    boolean existsByPostAndUser(Post post, User user);

    // 게시글과 사용자로 좋아요 조회
    Optional<PostLike> findByPostAndUser(Post post, User user);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);
}
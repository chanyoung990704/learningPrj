package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.domain.base.UserAuditableEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "posts")
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Post extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 게시글 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_category_id")
    private PostCategory category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostLike> likes = new HashSet<>();

    @Column(nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 연관 관계 메서드
     */

    // 댓글 연관관계
    public void addComment(Comment comment) {
        StringBuilder error = new StringBuilder();
        if(comment == null){
            error.append("Comment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(comments.contains(comment)){
            error.append("Comment 객체가 Post의 Comment 컬렉션에 이미 포함되어있음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        StringBuilder error = new StringBuilder();
        if(comment == null){
            error.append("Comment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(!comments.contains(comment)){
            error.append("Comment 객체가 Post의 Comment 컬렉션에 존재하지 않음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        comments.remove(comment);
        comment.setPost(null);
    }

    // 파일 연관관계
    public void addAttachment(File attachment) {
        StringBuilder error = new StringBuilder();
        if(attachment == null){
            error.append("attachment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(files.contains(attachment)){
            error.append("attachment 객체가 Post의 attachments 컬렉션에 이미 포함되어있음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        files.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(File attachment) {
        StringBuilder error = new StringBuilder();
        if(attachment == null){
            error.append("attachment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(!files.contains(attachment)){
            error.append("attachment 객체가 Post의 attachments 컬렉션에 존재하지 않음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        files.remove(attachment);
        attachment.setPost(null);
    }

    // 좋아요 수 조회
    public int getLikeCount() {
        return likes.size();
    }

    // 좋아요 추가
    public void addLike(PostLike like) {
        if (like == null) {
            throw new IllegalArgumentException("Like cannot be null");
        }
        if (!likes.contains(like)) {
            likes.add(like);
            like.setPost(this);
        }
    }

    // 좋아요 제거
    public void removeLike(PostLike like) {
        if (like != null && likes.contains(like)) {
            likes.remove(like);
            like.setPost(null);
        }
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public Long getViewCount() {
        return viewCount;
    }
}

package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.UserAuditableEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "comments")

public class Comment extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 상위 댓글 참조 (답글인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 하위 댓글 목록 (답글들)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    // 좋아요 목록
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CommentLike> likes = new HashSet<>();

    @Transient
    @Builder.Default
    private boolean likedByCurrentUser = false;

    // 답글 추가 메서드
    public void addChildComment(Comment childComment) {
        if (childComment == this) {
            throw new IllegalArgumentException("자기 자신을 답글로 등록할 수 없습니다.");
        }
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(childComment);
        childComment.setParent(this);
    }

    // 부모 댓글 설정 메서드
    public void setParent(Comment parent) {
        this.parent = parent;
    }

    // 좋아요 수 조회
    public int getLikeCount() {
        return likes.size();
    }

    // 좋아요 추가
    public void addLike(CommentLike like) {
        if (like == null) {
            throw new IllegalArgumentException("Like cannot be null");
        }
        if (!likes.contains(like)) {
            likes.add(like);
            like.setComment(this);
        }
    }

    // 좋아요 제거
    public void removeLike(CommentLike like) {
        if (like != null && likes.contains(like)) {
            likes.remove(like);
            like.setComment(null);
        }
    }

    // 현재 사용자의 좋아요 여부 설정
    public void setLikedByCurrentUser(boolean liked) {
        this.likedByCurrentUser = liked;
    }

    // 현재 사용자의 좋아요 여부 확인
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
}

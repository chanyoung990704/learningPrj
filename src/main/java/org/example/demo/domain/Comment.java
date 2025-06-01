package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.UserAuditableEntity;

import java.util.ArrayList;
import java.util.List;

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

}

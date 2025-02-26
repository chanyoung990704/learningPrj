package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.domain.base.UserAuditableEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "post_categories")
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class PostCategory extends UserAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 카테고리 이름

    /**
     * 자기참조
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PostCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostCategory> children = new ArrayList<>();


    /**
     * 연관관계 메서드
     */

    public void addChild(PostCategory child) {
        if (child == null) {
            log.warn("Child Category 객체가 null 값을 가진다.");
            throw new IllegalArgumentException("Child Category 객체가 null 값을 가질 수 없습니다.");
        }
        if (children.contains(child)) {
            log.warn("Child Category 객체가 이미 자식 컬렉션에 포함되어 있습니다.");
            throw new IllegalStateException("Child Category 객체가 이미 자식 컬렉션에 포함되어 있습니다.");
        }
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(PostCategory child) {
        if (child == null) {
            log.warn("Child Category 객체가 null 값을 가진다.");
            throw new IllegalArgumentException("Child Category 객체가 null 값을 가질 수 없습니다.");
        }
        if (!children.contains(child)) {
            log.warn("Child Category 객체가 자식 컬렉션에 존재하지 않습니다.");
            throw new IllegalStateException("Child Category 객체가 자식 컬렉션에 존재하지 않습니다.");
        }
        children.remove(child);
        child.setParent(null);
    }
}

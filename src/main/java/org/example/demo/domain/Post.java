package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "POST")
@Slf4j
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 게시글 작성자

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileAttachment> attachments = new ArrayList<>();

    @Builder
    public Post(String title, String content, Member member) {
        this.title = title;
        this.content = content;
        this.member = member;
    }

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
    public void addAttachment(FileAttachment attachment) {
        StringBuilder error = new StringBuilder();
        if(attachment == null){
            error.append("attachment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(attachments.contains(attachment)){
            error.append("attachment 객체가 Post의 attachments 컬렉션에 이미 포함되어있음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(FileAttachment attachment) {
        StringBuilder error = new StringBuilder();
        if(attachment == null){
            error.append("attachment 객체가 null 값을 가진다.\n");
            log.warn(error.toString());
            throw new IllegalArgumentException(error.toString());
        }
        if(!attachments.contains(attachment)){
            error.append("attachment 객체가 Post의 attachments 컬렉션에 존재하지 않음\n");
            log.warn(error.toString());
            throw new IllegalStateException(error.toString());
        }
        attachments.remove(attachment);
        attachment.setPost(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(getId(), post.getId()) && Objects.equals(getTitle(), post.getTitle()) && Objects.equals(getContent(), post.getContent()) && Objects.equals(getMember(), post.getMember()) && Objects.equals(getComments(), post.getComments()) && Objects.equals(getAttachments(), post.getAttachments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getContent(), getMember(), getComments(), getAttachments());
    }
}

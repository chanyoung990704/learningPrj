package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FILE_ATTACHMENT")
public class FileAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 원본 파일 이름
    @Column(nullable = false)
    private String originalName;

    // 서버에 저장된 파일 이름
    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private Long size; // File size in bytes

    @Column(nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public FileAttachment(String originalName, String storedName, Long size, String contentType, Post post) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.size = size;
        this.contentType = contentType;
        this.post = post;
    }
}

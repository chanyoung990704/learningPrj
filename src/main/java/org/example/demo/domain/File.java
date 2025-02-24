package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.UserAuditableEntity;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "files")
public class File extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String originalName; // 원본 파일명

    @Column(nullable = false)
    private String storedName; // 서버 저장 경로

    @Column(nullable = false)
    private Long fileSize; // 파일 크기


    @Builder
    public File(String originalName, String storedName, Long fileSize) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileSize = fileSize;
    }
}

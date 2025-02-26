package org.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.domain.base.UserAuditableEntity;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "files")
@EqualsAndHashCode(callSuper = false)
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
    private String storedName; // 서버 저장 이름

    @Column(nullable = false)
    private Long fileSize; // 파일 크기

    @Column(nullable = false)
    private String fileType; // 파일 타입

}

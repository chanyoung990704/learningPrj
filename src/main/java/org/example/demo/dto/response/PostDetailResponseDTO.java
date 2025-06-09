package org.example.demo.dto.response;

import lombok.*;
import org.example.demo.domain.File;
import org.example.demo.domain.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostDetailResponseDTO {

    Long id;           // 게시글 ID
    String title;      // 제목
    PostCategory category;   // 카테고리
    String author;     // 작성자
    String email;      // 가입 이메일
    LocalDateTime time; // 수정 시간
    String content;     // 본문 내용
    @Builder.Default
    Long likeCount = 0L; // 좋아요 수
    @Builder.Default
    Long viewCount = 0L; // 보기 수


    @Builder.Default
    Page<CommentResponseDTO> comments = new PageImpl<>(new ArrayList<>()); // 댓글
    @Builder.Default
    List<File> imageAttachments = new ArrayList<>(); // 이미지 파일
    @Builder.Default
    List<File> otherAttachments = new ArrayList<>(); // 그외 파일


}

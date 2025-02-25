package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.domain.PostCategory;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponseDTO {

    private Long id;           // 게시글 ID
    private String title;      // 제목
    private PostCategory category;   // 카테고리
    private String author;     // 작성자
    private LocalDateTime time; // 수정 시간
    private String content;     // 본문 내용
}

package org.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostListResponseDTO {

    Long id;
    String title;
    String author;
    LocalDateTime time;
    String categoryName;
    Long likeCount;
    Long viewCount;

    public PostListResponseDTO(Long id, String title, String author, LocalDateTime time, String categoryName, Long viewCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.time = time;
        this.categoryName = categoryName;
        this.viewCount = viewCount;
    }
}

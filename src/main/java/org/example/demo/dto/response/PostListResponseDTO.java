package org.example.demo.dto.response;

import lombok.*;
import org.example.demo.domain.PostCategory;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostListResponseDTO {

    Long id;
    String title;
    String content;
    String author;
    LocalDateTime time;
    PostCategory category;

    // 페이징
    int currentPage;
    int totalPages;
    long totalElements;
    int size;

}

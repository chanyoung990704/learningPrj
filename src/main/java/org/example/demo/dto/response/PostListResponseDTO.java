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
    String author;
    LocalDateTime time;
    PostCategory category;
}

package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.demo.domain.PostCategory;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostListResponseDTO {

    Long id;
    String title;
    String content;
    String author;
    LocalDateTime time;
    PostCategory category;

}

package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.demo.domain.Post;

@Data
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;

    PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
    }

}
package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;

@Data
public class PostRequestDTO {

    @NotBlank
    private String title;
    @NotBlank
    private String content;

    private Long categoryId;

    public static Post toPost(PostRequestDTO dto, User user, PostCategory postCategory) {
        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(user)
                .category(postCategory)
                .build();
    }

}

package org.example.demo.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class PostCreationRequestDTO {

    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    @NotNull
    private Long categoryId;

    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();

    public static Post toPost(PostCreationRequestDTO dto, User user, PostCategory postCategory) {
        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(user)
                .category(postCategory)
                .build();
    }

}

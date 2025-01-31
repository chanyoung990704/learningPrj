package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.demo.domain.Comment;

@Data
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;

    CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
    }
}
package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String userName;
    private String userEmail;
    private Long userId;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentResponseDTO> replies = new ArrayList<>();  // 답글 목록

    public CommentResponseDTO(Long id, String content, String userName, String userEmail, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static CommentResponseDTO toDtoWithChildren(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userName(comment.getUser().getName())
                .userEmail(comment.getUser().getEmail())
                .userId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getChildren()
                        .stream()
                        .map(CommentResponseDTO::toDtoWithChildren).toList()
                )
                .build();
    }

}
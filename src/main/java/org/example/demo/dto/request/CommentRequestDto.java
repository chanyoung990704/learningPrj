package org.example.demo.dto.request;

import lombok.Data;

@Data
public class CommentRequestDto {
    private Long postId;
    private Long memberId;
    private String content;
}
package org.example.demo.dto.request;

import lombok.Data;

@Data
public class PostRequestDto {
    private String title;
    private String content;
    private Long memberId;
}
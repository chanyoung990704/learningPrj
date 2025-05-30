package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponseDTO {
    private Long id;
    private String content;
    private String userName;
    private String userEmail;
    private Long userId;
    private LocalDateTime createdAt;
}
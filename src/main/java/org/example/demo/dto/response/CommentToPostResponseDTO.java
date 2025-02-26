package org.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentToPostResponseDTO {

    Long id;    // 댓글 id
    String content; // 댓글 내용
    String author; // 댓글 작성자 username
    String email; // 댓글 작성자 email
    LocalDateTime time; // 댓글 최종 수정 시간

}

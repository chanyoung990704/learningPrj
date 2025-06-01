package org.example.demo.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentCreationRequestDTO {
    // "CommentToPostRequest" -> "CommentCreationRequest" 로 rename
    @NotEmpty
    String content;

}

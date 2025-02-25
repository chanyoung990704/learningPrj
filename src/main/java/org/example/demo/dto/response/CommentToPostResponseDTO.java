package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentToPostResponseDTO {

    String content;
    String author;
    LocalDateTime time;

}

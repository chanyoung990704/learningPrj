package org.example.demo.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Getter
public class ErrorResponse {
    private final String title;
    private final String detail;
    private final int status;

    @Builder.Default
    private final String timestamp = LocalDateTime.now().toString();
}

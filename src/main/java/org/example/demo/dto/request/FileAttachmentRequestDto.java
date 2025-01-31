package org.example.demo.dto.request;

import lombok.Data;

@Data
public class FileAttachmentRequestDto {
    private Long postId;
    private String originalName;
    private String storedName;
    private Long size;
    private String contentType;
}
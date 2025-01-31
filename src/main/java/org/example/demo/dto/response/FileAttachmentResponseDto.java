package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.demo.domain.FileAttachment;

@Data
@AllArgsConstructor
public class FileAttachmentResponseDto {
    private Long id;
    private String originalName;
    private String storedName;
    private Long size;
    private String contentType;

    FileAttachmentResponseDto(FileAttachment fileAttachment) {
        this.id = fileAttachment.getId();
        this.originalName = fileAttachment.getOriginalName();
        this.storedName = fileAttachment.getStoredName();
        this.size = fileAttachment.getSize();
        this.contentType = fileAttachment.getContentType();
    }
}
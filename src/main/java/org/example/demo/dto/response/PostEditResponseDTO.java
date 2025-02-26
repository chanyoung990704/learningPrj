package org.example.demo.dto.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.demo.domain.File;
import org.example.demo.domain.PostCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostEditResponseDTO {

    Long id;
    @NotEmpty
    String title;
    @NotEmpty
    String content;
    @NotNull
    PostCategory category;

    @Builder.Default
    List<File> attachments = new ArrayList<>(); // 기존에 업로드된 파일 리스트
    @Builder.Default
    List<MultipartFile> newAttachments = new ArrayList<>(); // 새로 추가한 파일
    @Builder.Default
    List<Long> deletedAttachmentsId = new ArrayList<>(); // 삭제할 파일 아이디

}

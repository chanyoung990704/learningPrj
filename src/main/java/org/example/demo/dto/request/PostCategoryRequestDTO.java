package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCategoryRequestDTO {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    private Long parentId; // 부모 카테고리

}

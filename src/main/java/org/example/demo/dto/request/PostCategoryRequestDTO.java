package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostCategoryRequestDTO {

    @NotEmpty
    private String name;

    private Long parentId; // 부모 카테고리

}

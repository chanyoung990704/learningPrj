package org.example.demo.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostSearchRequestDTO {
    @Builder.Default
    private String searchType = "title";  // "title", "author", "category"
    private String searchText;
}

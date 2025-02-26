package org.example.demo.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostSearchRequestDTO {

    String title;

}

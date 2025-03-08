package org.example.demo.events;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SignUpEvent {

    private String email;
    private String username;
}

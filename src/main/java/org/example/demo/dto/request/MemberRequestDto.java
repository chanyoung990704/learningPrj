package org.example.demo.dto.request;

import lombok.Data;
import org.example.demo.domain.Role;

@Data
public class MemberRequestDto {
    private String username;
    private String password;
    private Role role;

}
package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.demo.domain.Member;
import org.example.demo.domain.Role;

@Data
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String username;
    private Role role;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.role = member.getRole();
    }
}
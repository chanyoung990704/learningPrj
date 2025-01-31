package org.example.demo.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Member;
import org.example.demo.domain.Role;
import org.example.demo.dto.request.MemberRequestDto;
import org.example.demo.dto.response.MemberResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("없는 ID의 멤버 조회시 예외")
    void notFindById() {
        // when&then
        assertThrows(EntityNotFoundException.class, () -> memberService.findById(12345L));
    }

    @Test
    @DisplayName("DTO 사용 저장 성공, 및 DTO 반환 Role은 null일 때 Role.User로 저장")
    void saveMemberDTOWithNotRole() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto();
        requestDto.setUsername("test");
        String pwd = "password123";
        requestDto.setPassword(pwd);

        // when
        MemberResponseDto responseDto = memberService.save(requestDto);

        // then
        Member savedMember = memberService.findByUsername(requestDto.getUsername());
        // repository와 RequestDTO비교
        assertNotNull(savedMember);
        assertNotNull(savedMember.getId());
        assertEquals(requestDto.getUsername(), savedMember.getUsername());
        assertTrue(passwordEncoder.matches(requestDto.getPassword(), savedMember.getPassword()));
        assertEquals(Role.ROLE_USER, savedMember.getRole());
        assertNotNull(savedMember.getCreatedAt());
        assertNotNull(savedMember.getUpdatedAt());

        // repository와 responseDTO 비교
        assertEquals(responseDto.getId(), savedMember.getId());
        assertEquals(responseDto.getUsername(), savedMember.getUsername());
        assertEquals(responseDto.getRole(), savedMember.getRole());

    }
}
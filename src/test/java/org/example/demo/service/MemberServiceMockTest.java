package org.example.demo.service;

import org.example.demo.domain.Member;
import org.example.demo.dto.request.MemberRequestDto;
import org.example.demo.dto.response.MemberResponseDto;
import org.example.demo.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceMockTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("MemberRequestDto 파라미터 저장 성공 테스트")
    void test() {
        // given
        // DTO
        MemberRequestDto requestDto = new MemberRequestDto();
        requestDto.setUsername("testMember");
        requestDto.setPassword("12345678");

        String encodedPassword = "encoded12345678";

        // Member
        Member member = Member.builder()
                .username(requestDto.getUsername())
                .password(encodedPassword)
                .build();

        member.setId(1L);

        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(encodedPassword);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        MemberResponseDto responseDto = memberService.save(requestDto);

        // then
        assertEquals(member.getId(), responseDto.getId());
        assertEquals(member.getUsername(), responseDto.getUsername());
        // passwordEncoding 검증
        verify(passwordEncoder).encode(requestDto.getPassword());
        verify(memberRepository, times(1)).save(any(Member.class));

    }
}
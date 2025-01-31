package org.example.demo.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Member;
import org.example.demo.dto.request.MemberRequestDto;
import org.example.demo.dto.response.MemberResponseDto;
import org.example.demo.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponseDto save(MemberRequestDto requestDto) {
        Member member = createMember(requestDto);
        Member saved = memberRepository.save(member);
        return new MemberResponseDto(saved);
    }

    private Member createMember(MemberRequestDto requestDto) {
        Member member = Member.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .build();
        return member;
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없음 id: " + id));
    }

    public Member findByUsername(String username){
        return memberRepository.findMemberByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없음 username: " + username));
    }

}

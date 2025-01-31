package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Member;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostRequestDto;
import org.example.demo.repository.MemberRepository;
import org.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void save(PostRequestDto requestDto) {
        Post post = createPost(requestDto);
        postRepository.save(post);
    }

    

    private Post createPost(PostRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId()).get();
        return Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .member(member)
                .build();
    }
}

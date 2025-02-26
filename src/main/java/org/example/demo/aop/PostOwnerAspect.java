package org.example.demo.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class PostOwnerAspect {


    private final PostService postService;

    @Pointcut("@annotation(org.example.demo.aop.VerifyPostOwner)")
    public void verifyPostOwnerPointcut() {
    }

    @Before("verifyPostOwnerPointcut() && args(.., postId, ..)")
    public void verifyCommentOwner(JoinPoint joinPoint, Long postId) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // 게시글 작성자 확인
        Post post = postService.findPostWithUser(postId);

        if (!post.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글 작성자가 아닙니다.");
        }
    }
}

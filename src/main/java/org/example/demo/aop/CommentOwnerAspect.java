package org.example.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.demo.domain.Comment;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.service.CommentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class CommentOwnerAspect {

    private final CommentService commentService; // 댓글 정보를 가져오는 서비스

    @Pointcut("@annotation(org.example.demo.aop.VerifyCommentOwner)")
    public void verifyCommentOwnerPointcut() {
    }

    @Before("verifyCommentOwnerPointcut() && args(.., commentId, ..)")
    public void verifyCommentOwner(JoinPoint joinPoint, Long commentId) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        Object principal = authentication.getPrincipal();
        String email;
        
        // CustomUserDetails를 통해 이메일 추출
        if (principal instanceof CustomUserDetails) {
            email = ((CustomUserDetails) principal).getEmail();
        } else {
            throw new AccessDeniedException("지원하지 않는 인증 유형입니다: " + principal.getClass().getName());
        }

        // 댓글 작성자 확인
        Comment comment = commentService.findCommentByIdWithUser(commentId);

        if (!comment.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("댓글 작성자가 아닙니다.");
        }
    }
}

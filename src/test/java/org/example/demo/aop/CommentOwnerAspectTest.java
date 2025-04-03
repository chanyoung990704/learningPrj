package org.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Comment;
import org.example.demo.domain.User;
import org.example.demo.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTestWithProfile("test")
@Transactional
class CommentOwnerAspectTest {


    @Mock
    private CommentService commentService;

    @Mock
    private JoinPoint joinPoint;

    @InjectMocks
    private CommentOwnerAspect aspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("SecurityContext유저와 댓글 소유자가 다른 경우 예외")
    void verifyCommentOwner_ValidOwner_ExceptionThrown() {
        // g
        Long commentId = 1L;
        String name = "tester";
        String email = "test@example.com";
        String password = "test";

        // UserDetail 생성 후 권한 저장
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(email, password, Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(auth);


        User testUser = User.builder().name(name)
                .email(email)
                .password(password).build();

        User notOwner = User.builder().name("noname")
                .email("noname@example.com").build();

        Comment testComment = Comment.builder()
                .id(commentId)
                .user(notOwner)
                .content("test").build();

        when(commentService.findCommentByIdWithUser(commentId)).thenReturn(testComment);

        // w & t
        assertThrows(AccessDeniedException.class, () -> aspect.verifyCommentOwner(joinPoint, commentId));

    }

    @Test
    @DisplayName("SecurityContext유저와 댓글 소유자가 같을 경우 정상 동작")
    void verifyCommentOwner_ValidOwner_NotExceptionThrown() {
        // g
        Long commentId = 1L;
        String name = "tester";
        String email = "test@example.com";
        String password = "test";

        // UserDetail 생성 후 권한 저장
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(email, password, Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(auth);


        User testUser = User.builder().name(name)
                .email(email)
                .password(password).build();

        Comment testComment = Comment.builder()
                .id(commentId)
                .user(testUser)
                .content("test").build();

        when(commentService.findCommentByIdWithUser(commentId)).thenReturn(testComment);

        // w & t
        assertDoesNotThrow(() -> aspect.verifyCommentOwner(joinPoint, commentId));
    }
}
package org.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.*;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
@Transactional
@DisplayName("CommentServiceImpl 통합 테스트")
class CommentServiceImplTest {

    @Autowired
    private CommentService commentServiceImpl;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("댓글 저장 및 조회 테스트")
    void saveAndFindCommentTest() {
        // g
        Address address = new Address("서울", "강남구", "테헤란로");
        User user = User.builder()
                .name("John Doe")
                .email("johndoe@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .user(user)
                .build();
        postService.save(post);

        Comment comment = Comment.builder()
                .content("This is a test comment.")
                .user(user)
                .build();
        post.addComment(comment);

        // w
        Long savedCommentId = commentServiceImpl.save(comment);
        em.flush(); // DB 동기화
        em.clear(); // 영속성 컨텍스트 초기화

        Comment savedComment = commentServiceImpl.findById(savedCommentId);

        // t
        assertEquals("This is a test comment.", savedComment.getContent());
        assertEquals(user.getEmail(), savedComment.getUser().getEmail());
        assertEquals(post.getTitle(), savedComment.getPost().getTitle());
    }

    @Test
    @DisplayName("댓글 저장 - 게시글과 사용자 연관 테스트")
    void saveCommentWithPostAndUserTest() {
        // g
        Address address = new Address("서울", "강동구", "천호대로");
        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        Post post = Post.builder()
                .title("Another Test Post")
                .content("Another test content.")
                .user(user)
                .build();
        postService.save(post);

        CommentCreationRequestDTO requestDTO = CommentCreationRequestDTO.builder()
                .content("A comment for the post.")
                .build();

        // w
        Long commentId = commentServiceImpl.save(requestDTO, user.getEmail(), post.getId());
        em.flush();
        em.clear();

        Comment savedComment = commentServiceImpl.findById(commentId);

        // t
        assertEquals("A comment for the post.", savedComment.getContent());
        assertEquals(user.getEmail(), savedComment.getUser().getEmail());
        assertEquals("Another Test Post", savedComment.getPost().getTitle());
    }

    @Test
    @DisplayName("게시글 ID로 댓글 조회 테스트")
    void findCommentsByPostIdTest() {
        // g
        Address address = new Address("서울", "마포구", "합정로");
        User user = User.builder()
                .name("Jane Doe")
                .email("janedoe@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        Post post = Post.builder()
                .title("Post with Comments")
                .content("A post with multiple comments.")
                .user(user)
                .build();
        postService.save(post);

        Comment comment1 = Comment.builder()
                .content("First comment")
                .user(user)
                .build();
        Comment comment2 = Comment.builder()
                .content("Second comment")
                .user(user)
                .build();
        post.addComment(comment1);
        post.addComment(comment2);

        commentServiceImpl.save(comment1);
        commentServiceImpl.save(comment2);
        em.flush();
        em.clear();

        // w
        List<Comment> comments = commentServiceImpl.findCommentsByPostIdWithUserAndPost(post.getId());

        // t
        assertEquals(2, comments.size());
        assertTrue(comments.stream().anyMatch(comment -> comment.getContent().equals("First comment")));
        assertTrue(comments.stream().anyMatch(comment -> comment.getContent().equals("Second comment")));
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteCommentTest() {
        // g
        Address address = new Address("대구", "중구", "중앙대로");
        User user = User.builder()
                .name("David")
                .email("david@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        Post post = Post.builder()
                .title("Post to Delete Comment")
                .content("A post to test comment deletion.")
                .user(user)
                .build();
        postService.save(post);

        Comment comment = Comment.builder()
                .content("Comment to delete.")
                .user(user)
                .build();
        post.addComment(comment);

        Long commentId = commentServiceImpl.save(comment);
        em.flush();
        em.clear();

        // w
        commentServiceImpl.deleteById(commentId);

        // t
        assertThrows(RuntimeException.class, () -> commentServiceImpl.findById(commentId));
    }

    @Test
    @DisplayName("댓글 업데이트 테스트")
    void updateCommentTest() {
        // g
        Address address = new Address("부산", "해운대구", "해운대로");
        User user = User.builder()
                .name("Eve")
                .email("eve@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        Post post = Post.builder()
                .title("Post for Update Test")
                .content("Content for update test.")
                .user(user)
                .build();
        postService.save(post);

        Comment comment = Comment.builder()
                .content("Original Comment Content")
                .user(user)
                .build();

        post.addComment(comment);

        Long commentId = commentServiceImpl.save(comment);

        Comment updatedComment = Comment.builder()
                .content("Updated Comment Content")
                .build();


        em.flush();
        em.clear();

        // w
        commentServiceImpl.update(commentId, updatedComment);

        em.flush();
        em.clear();

        Comment updated = commentServiceImpl.findById(commentId);

        // t
        assertEquals("Updated Comment Content", updated.getContent());
    }
}

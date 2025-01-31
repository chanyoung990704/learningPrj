package org.example.demo.domain;

import org.example.demo.SpringBootTestWithProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    @DisplayName("Post 엔티티와 Comment 연관관계 매핑 성공 테스트")
    void addCommentOnPost() {
        Member testMember = Member.builder()
                .username("testMember")
                .password("testPassword")
                .build();

        Post testPost = Post.builder()
                .title("testTitle")
                .member(testMember)
                .content("testContent")
                .build();

        Comment testComment = Comment.builder()
                .content("testContent")
                .build();

        // 연관관계 추가
        testPost.addComment(testComment);

        // post -> Comment 검증
        assertTrue(testPost.getComments().contains(testComment));
        // Comment -> post 검증
        assertTrue(testComment.getPost().equals(testPost));
    }

    @Test
    @DisplayName("addComment 메서드 - null Comment 예외 처리 테스트")
    void addNullCommentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .content("testContent")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            testPost.addComment(null);
        });

        assertEquals("Comment 객체가 null 값을 가진다.\n", exception.getMessage());
    }

    @Test
    @DisplayName("addComment 메서드 - 중복 Comment 예외 처리 테스트")
    void addDuplicateCommentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .content("testContent")
                .build();

        Comment testComment = Comment.builder()
                .content("testContent")
                .build();

        // 첫번째 Add
        testPost.addComment(testComment);

        // 중복 Add
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            testPost.addComment(testComment);
        });

        assertEquals("Comment 객체가 Post의 Comment 컬렉션에 이미 포함되어있음\n", exception.getMessage());
    }

    @Test
    @DisplayName("removeComment 메서드 - null Comment 예외 처리 테스트")
    void removeNullCommentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .content("testContent")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            testPost.removeComment(null);
        });

        assertEquals("Comment 객체가 null 값을 가진다.\n", exception.getMessage());
    }

    @Test
    @DisplayName("removeComment 메서드 - 존재하지 않는 Comment 예외 처리 테스트")
    void removeNonExistentCommentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .content("testContent")
                .build();

        Comment nonExistentComment = Comment.builder()
                .content("nonExistentContent")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            testPost.removeComment(nonExistentComment);
        });

        assertEquals("Comment 객체가 Post의 Comment 컬렉션에 존재하지 않음\n", exception.getMessage());
    }

    @Test
    @DisplayName("Post 엔티티와 FileAttachment 연관관계 매핑 성공 테스트")
    void addAttachmentOnPost() {
        Member testMember = Member.builder()
                .username("testMember")
                .password("testPassword")
                .build();

        Post testPost = Post.builder()
                .title("testTitle")
                .content("testContent")
                .member(testMember)
                .build();

        FileAttachment testAttachment = FileAttachment.builder()
                .originalName("original.txt")
                .storedName("stored-unique.txt")
                .size(1024L)
                .contentType("text/plain")
                .build();

        testPost.addAttachment(testAttachment);

        assertTrue(testPost.getAttachments().contains(testAttachment));
        assertEquals(testPost, testAttachment.getPost());
    }

    @Test
    @DisplayName("addAttachment 메서드 - null FileAttachment 예외 처리 테스트")
    void addNullAttachmentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .content("testContent")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            testPost.addAttachment(null);
        });
        assertEquals("attachment 객체가 null 값을 가진다.\n", exception.getMessage());
    }

    @Test
    @DisplayName("addAttachment 메서드 - 중복 FileAttachment 예외 처리 테스트")
    void addDuplicateAttachmentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .content("testContent")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .build();

        FileAttachment testAttachment = FileAttachment.builder()
                .originalName("original.txt")
                .storedName("stored-unique.txt")
                .size(1024L)
                .contentType("text/plain")
                .build();

        testPost.addAttachment(testAttachment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            testPost.addAttachment(testAttachment);
        });
        assertEquals("attachment 객체가 Post의 attachments 컬렉션에 이미 포함되어있음\n", exception.getMessage());
    }

    @Test
    @DisplayName("removeAttachment 메서드 - null FileAttachment 예외 처리 테스트")
    void removeNullAttachmentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .content("testContent")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            testPost.removeAttachment(null);
        });
        assertEquals("attachment 객체가 null 값을 가진다.\n", exception.getMessage());
    }

    @Test
    @DisplayName("removeAttachment 메서드 - 존재하지 않는 FileAttachment 예외 처리 테스트")
    void removeNonExistentAttachmentThrowsException() {
        Post testPost = Post.builder()
                .title("testTitle")
                .content("testContent")
                .member(Member.builder()
                        .username("testMember")
                        .password("testPassword")
                        .build())
                .build();

        FileAttachment nonExistentAttachment = FileAttachment.builder()
                .originalName("nonexistent.txt")
                .storedName("nonexistent-stored.txt")
                .size(512L)
                .contentType("text/plain")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            testPost.removeAttachment(nonExistentAttachment);
        });
        assertEquals("attachment 객체가 Post의 attachments 컬렉션에 존재하지 않음\n", exception.getMessage());
    }
}

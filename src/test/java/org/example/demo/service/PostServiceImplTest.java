package org.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTestWithProfile("test")
@Transactional
class PostServiceImplTest {

    @Autowired
    private PostService postServiceImpl;

    @Autowired
    private UserService userServiceImpl;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("게시글 저장 테스트")
    void test() {

        User user = User.builder()
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        em.persist(user);
        em.flush();
        em.clear();


        Post post = Post.builder()
                .title("testTitle")
                .content("testContent")
                .user(user)
                .build();

        Long save = postServiceImpl.save(post);

        assertNotNull(save);
    }
}
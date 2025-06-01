package org.example.demo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.repository.PostCategoryRepository;
import org.example.demo.repository.PostRepository;
import org.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTestWithProfile("test")
@Transactional
public class PostApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostCategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private PostCategory testCategory;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Test User")
                .build();
        userRepository.save(testUser);

        // Create test category
        testCategory = PostCategory.builder()
                .name("Test Category")
                .build();
        categoryRepository.save(testCategory);

        // Create test posts
        Post post1 = Post.builder()
                .title("Test Post 1")
                .content("Test Content 1")
                .user(testUser)
                .category(testCategory)
                .build();
        
        Post post2 = Post.builder()
                .title("Test Post 2")
                .content("Test Content 2")
                .user(testUser)
                .category(testCategory)
                .build();
        
        postRepository.save(post1);
        postRepository.save(post2);
    }

    @Test
    @DisplayName("API 게시글 생성 통합 테스트")
    @WithMockUser(username = "test@example.com")
    void testCreatePost() throws Exception {
        // Given
        PostCreationRequestDTO requestDTO = PostCreationRequestDTO.builder()
                .title("New Post")
                .content("New Content")
                .categoryId(testCategory.getId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated());

        // 저장 검증
        List<Post> posts = postRepository.findPostsWithUserAndCategory();
        Map<String, List<Post>> groupByTitle = posts.stream().collect(Collectors.groupingBy(Post::getTitle));

        assertTrue(groupByTitle.containsKey("New Post"));
    }

}
package org.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.*;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.impl.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * given when then 템플릿의 통합 테스트
 * given -> g
 * when -> w
 * then -> t
 */
@SpringBootTestWithProfile("test")
@Transactional
@DisplayName("PostServiceImpl 통합 테스트")
class PostServiceImplTest {

    @Autowired
    private PostService postServiceImpl;

    @Autowired
    private UserService userService;

    @Autowired
    private PostCategoryService postCategoryService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FileService fileService;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("게시글 저장 및 조회 테스트")
    void saveAndFindPostTest() {
        // g
        Address address = new Address("서울", "강남구", "테헤란로");
        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        PostCategory category = PostCategory.builder()
                .name("카테고리명").build();
        postCategoryService.save(category);

        File file = File.builder()
                .storedName("stored")
                .originalName("org")
                .fileType("img")
                .fileSize(123L).build();

        Post post = Post.builder()
                .title("Sample Post")
                .content("This is a test post.")
                .user(user)
                .category(category)
                .build();

        // 연관관계
        post.addAttachment(file);

        // w
        Long savedPostId = postServiceImpl.save(post);
        em.flush(); // DB에 동기화
        em.clear(); // 영속성 컨텍스트 초기화

        Post savedPost = postServiceImpl.findById(savedPostId);

        // t
        assertEquals(post.getTitle(), savedPost.getTitle());
        assertEquals(post.getContent(), savedPost.getContent());
        assertEquals(user.getEmail(), savedPost.getUser().getEmail());
        assertEquals(category.getName(), savedPost.getCategory().getName());
        assertEquals(1, savedPost.getFiles().size());
        assertEquals(file.getStoredName(), savedPost.getFiles().get(0).getStoredName());
        assertEquals(file.getOriginalName(), savedPost.getFiles().get(0).getOriginalName());
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePostTest() {
        // g
        Address address = new Address("서울", "은평구", "녹번로");
        User user = User.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        PostCategory category = PostCategory.builder()
                .name("카테고리명").build();
        postCategoryService.save(category);


        Post post = Post.builder()
                .title("Delete Post Test")
                .content("This is a delete test.")
                .user(user)
                .category(category)
                .build();

        Long savedPostId = postServiceImpl.save(post);
        em.flush();
        em.clear();

        // w
        postServiceImpl.deleteById(savedPostId);

        // t
        assertThrows(RuntimeException.class, () -> postServiceImpl.findById(savedPostId));
    }

    @Test
    @DisplayName("게시글 업데이트 테스트")
    void updatePostTest() {
        // g
        Address address = new Address("대전", "유성구", "대덕대로");
        User user = User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        PostCategory category = PostCategory.builder()
                .name("카테고리명").build();
        postCategoryService.save(category);


        Post post = Post.builder()
                .title("Original Title")
                .content("Original Content")
                .user(user)
                .category(category)
                .build();
        Long postId = postServiceImpl.save(post);


        PostEditResponseDTO updateDTO = PostEditResponseDTO.builder()
                .id(postId)
                .title("Updated Title")
                .content("Updated Content")
                .category(category)
                .build();

        em.flush();
        em.clear();

        // w
        postServiceImpl.update(updateDTO);

        em.flush();
        em.clear();

        Post updatedPost = postServiceImpl.findById(postId);

        // t
        assertEquals("Updated Title", updatedPost.getTitle());
        assertEquals("Updated Content", updatedPost.getContent());
        assertEquals(user.getEmail(), updatedPost.getUser().getEmail());
        assertEquals(category.getName(), updatedPost.getCategory().getName());
    }

    @Test
    @DisplayName("카테고리와 사용자 정보를 포함한 게시글 조회 테스트")
    void findPostsWithUserAndCategoryTest() {
        // g
        Address address = new Address("서울", "서대문구", "홍제로");
        User user = User.builder()
                .name("Dave")
                .email("dave@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        PostCategory category = PostCategory.builder()
                .name("테스트 카테고리").build();
        postCategoryService.save(category);

        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test content.")
                .user(user)
                .category(category)
                .build();
        postServiceImpl.save(post);

        em.flush();
        em.clear();

        // w
        List<Post> posts = postServiceImpl.findPostsWithUserAndCategory();

        // t
        assertNotNull(posts);
        assertTrue(posts.size() > 0);
        Post retrievedPost = posts.get(0);
        assertEquals("Test Post", retrievedPost.getTitle());
        assertEquals("테스트 카테고리", retrievedPost.getCategory().getName());
        assertEquals("Dave", retrievedPost.getUser().getName());
    }

    @Test
    @DisplayName("게시글 검색 테스트")
    void searchPostsTest() {
        // g
        Address address = new Address("서울", "중랑구", "면목로");
        User user = User.builder()
                .name("Eve")
                .email("eve@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .address(address)
                .build();
        userService.save(user);

        PostCategory category = PostCategory.builder()
                .name("검색 카테고리").build();

        postCategoryService.save(category);

        Post post = Post.builder()
                .title("Search Test Post")
                .content("Content about search.")
                .user(user)
                .category(category)
                .build();
        postServiceImpl.save(post);

        PostSearchRequestDTO searchRequestDTO = PostSearchRequestDTO.builder()
                .searchText("Search")
                .build();

        em.flush();
        em.clear();

        // w
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> searchResults = postServiceImpl.searchPosts(searchRequestDTO, pageable);

        // t
        assertNotNull(searchResults);
        assertEquals(1, searchResults.getTotalElements());
        Post resultPost = searchResults.getContent().get(0);
        assertEquals("Search Test Post", resultPost.getTitle());
    }
}

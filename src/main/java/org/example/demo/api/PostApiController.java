package org.example.demo.api;

import lombok.*;
import org.example.demo.aop.TimeTrace;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor

public class PostApiController {

    private final PostService postService;
    private final CommentService commentService;


    // 게시글 생성
    @PostMapping
    public ResponseEntity<Long> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Validated @RequestBody PostRequestDTO postRequestDTO) {

        Long postId = postService.save(postRequestDTO, userDetails.getUsername(), postRequestDTO.getCategoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    // 게시글 리스트 가져오기
    @GetMapping
    @TimeTrace
    public ResponseEntity<PostApiData<?>> getPosts(
            @ModelAttribute PostSearchRequestDTO searchRequestDTO,
            @PageableDefault(sort = {"createdAt"}, size = PostRepository.DEFAULT_PAGE_SIZE, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Post> postPage = postService.searchPosts(searchRequestDTO, pageable);
        List<Post> posts = postPage.getContent();

        // DTO 변환
        List<PostApiDetailResponseDTO> dto = posts.stream()
                .map(i -> PostApiDetailResponseDTO.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .content(i.getContent())
                        .time(i.getUpdatedAt())
                        .author(i.getUser().getName()).build()).collect(Collectors.toList());


        // API 전용 DATA 클래스 래핑
        PostApiData<List<PostApiDetailResponseDTO>> listPostApiData = new PostApiData<>(dto);

        return ResponseEntity.ok(listPostApiData);
    }


    @Data
    @NoArgsConstructor
    static class PostApiData<T extends Collection> {
        T data;
        int cnt;

        public PostApiData(T data) {
            this.data = data;
            this.cnt = data.size();
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class PostApiDetailResponseDTO {
        Long id;
        String title;
        String content;
        String author;
        LocalDateTime time;
    }
}

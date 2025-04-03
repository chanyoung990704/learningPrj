package org.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.TimeTrace;
import org.example.demo.aop.VerifyPostOwner;
import org.example.demo.domain.Comment;
import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.CommentToPostResponseDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.impl.FileService;
import org.example.demo.service.impl.FileStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final FileService fileService;
    private final FileStore fileStore;

    @GetMapping("/new")
    public String saveForm(@ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO) {
        return "post/post-form";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @Validated @ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response) throws IOException {

        if (bindingResult.hasErrors()) {
            return "post/post-form";
        }

        Long postId = postService.save(postRequestDTO, userDetails.getUsername(), postRequestDTO.getCategoryId());
        return "redirect:/posts/" + postId;
    }

    @GetMapping
    @TimeTrace
    public String showPostList(@ModelAttribute(value = "searchRequestDTO") PostSearchRequestDTO searchRequestDTO,
                               @PageableDefault(sort = {"createdAt"}, size = PostRepository.DEFAULT_PAGE_SIZE, direction = Sort.Direction.DESC) Pageable pageable,
                               Model model) {

        // 검색어 페이징
        Page<Post> postPage = postService.searchPosts(searchRequestDTO, pageable);
        List<Post> posts = postPage.getContent();

        // Response 변환
        List<PostListResponseDTO> responseDTOS = posts.stream()
                .map(post -> PostListResponseDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .author(post.getUser().getName())
                        .time(post.getUpdatedAt())
                        .currentPage(postPage.getNumber())
                        .totalPages(postPage.getTotalPages())
                        .totalElements(postPage.getTotalElements())
                        .size(pageable.getPageSize())
                        .category(post.getCategory()).build())
                .collect(Collectors.toList());

        model.addAttribute("posts", responseDTOS);

        return "post/post-list";

    }

    @GetMapping("/{postId}")
    public String showPostDetail(@PathVariable("postId") Long postId, Model model) {
        // 게시글 가져오기
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);
        // 게시글 댓글 가져오기
        List<Comment> comments = commentService.findCommentsByPostIdWithUserAndPost(post.getId());
        // Response DTO 변환
        PostDetailResponseDTO postResponseDTO = getPostDetailResponseDTO(post, comments);

        model.addAttribute("post", postResponseDTO);
        // 댓글 등록을 위한 폼 모델
        model.addAttribute("commentAddRequest", CommentToPostRequestDTO.builder().build());
        // 댓글 수정을 위한 폼 모델
        model.addAttribute("commentEditRequest", CommentToPostRequestDTO.builder().build());
        return "post/post-detail";
    }

    @GetMapping("/{postId}/edit")
    @VerifyPostOwner
    public String editForm(@PathVariable("postId") Long postId, Model model) {

        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);

        PostEditResponseDTO postEditResponseDTO = PostEditResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .attachments(post.getFiles())
                .build();

        model.addAttribute("postEditResponseDTO", postEditResponseDTO);

        return "post/post-edit";
    }

    @PutMapping("/{postId}")
    @VerifyPostOwner
    public String editPost(@PathVariable("postId") Long postId,
                           @Validated @ModelAttribute("postEditResponseDTO") PostEditResponseDTO responseDTO,
                           BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            // MultipartFile은 값이 초기화 되기 때문에 명시적으로 초기화문 선언
            responseDTO.getNewAttachments().clear();
            return "/post/post-edit";
        }

        // 게시글 업데이트
        postService.update(responseDTO);

        return "redirect:/posts/" + postId;
    }

    @DeleteMapping("/{postId}")
    @VerifyPostOwner
    public String deletePost(@PathVariable("postId") Long postId) {
        postService.deleteById(postId);
        return "redirect:/posts";
    }

    @ResponseBody
    @GetMapping("/uploads/images/{filename}")
    public Resource downloadImage(@PathVariable("filename") String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @ResponseBody
    @GetMapping("/uploads/files/{id}")
    public Resource downloadFile(@PathVariable("id") Long id,
                                 HttpServletResponse response) throws MalformedURLException {
        // 파일 가져오기
        File file = fileService.getFile(id);

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(file.getStoredName()));

        // 응답 헤더 설정
        String encodedOriginFileName = UriUtils.encode(file.getOriginalName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" +
                encodedOriginFileName + "\"";
        response.setHeader("Content-Disposition", contentDisposition);

        return resource;
    }

    public static PostDetailResponseDTO getPostDetailResponseDTO(Post post, List<Comment> comments) {
        /**
         * 첨부 파일 구분
         */
        Map<Boolean, List<File>> imageFile = post.getFiles().stream()
                .collect(Collectors.partitioningBy(file -> file.getFileType().startsWith("image")));

        /**
         * Entity -> DTO 변환
         */
        PostDetailResponseDTO postDetailResponseDTO = PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .time(post.getUpdatedAt())
                .category(post.getCategory())
                .author(post.getUser().getName())
                .email(post.getUser().getEmail())
                .comments(comments.stream().map(comment ->
                        CommentToPostResponseDTO.builder()
                                .id(comment.getId())
                                .content(comment.getContent())
                                .author(comment.getUser().getName())
                                .email(comment.getUser().getEmail())
                                .time(comment.getCreatedAt()).build()).collect(Collectors.toList()))
                .imageAttachments(imageFile.get(true))
                .otherAttachments(imageFile.get(false))
                .build();


        return postDetailResponseDTO;
    }
}

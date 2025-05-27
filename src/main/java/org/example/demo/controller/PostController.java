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
import org.example.demo.util.PostDetailUtil;
import org.example.demo.util.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // ====== 게시글 폼 및 저장 ======
    @GetMapping("/new")
    public String saveForm(@ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO) {
        return "post/post-form";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal Object principal,
                       @Validated @ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response) throws IOException {
        if (bindingResult.hasErrors()) {
            return "post/post-form";
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        Long postId = postService.save(postRequestDTO, email, postRequestDTO.getCategoryId());
        return "redirect:/posts/" + postId;
    }

    // ====== 게시글 목록 ======
    @GetMapping
    @TimeTrace
    public String showPostList(@ModelAttribute(value = "searchRequestDTO") PostSearchRequestDTO searchRequestDTO,
                               @PageableDefault(sort = {"createdAt"}, size = PostRepository.DEFAULT_PAGE_SIZE, direction = Sort.Direction.DESC) Pageable pageable,
                               Model model) {
        Page<PostListResponseDTO> postPage = postService.getPostList(searchRequestDTO, pageable);
        model.addAttribute("posts", postPage);
        return "post/post-list";
    }

    // ====== 게시글 상세 ======
    @GetMapping("/{postId}")
    public String showPostDetail(@PathVariable("postId") Long postId, 
                                 @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
                                 Model model) {
        PostDetailResponseDTO postResponseDTO = PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, commentPage);
        registerPostDetailModelAttributes(model, postResponseDTO);
        return "post/post-detail";
    }

    // ====== 게시글 수정 ======
    @GetMapping("/{postId}/edit")
    @VerifyPostOwner
    public String editForm(@PathVariable("postId") Long postId, Model model) {
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);
        PostEditResponseDTO postEditResponseDTO = toPostEditResponseDTO(post);
        model.addAttribute("postEditResponseDTO", postEditResponseDTO);
        return "post/post-edit";
    }

    @PutMapping("/{postId}")
    @VerifyPostOwner
    public String editPost(@PathVariable("postId") Long postId,
                           @Validated @ModelAttribute("postEditResponseDTO") PostEditResponseDTO responseDTO,
                           BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            responseDTO.getNewAttachments().clear();
            return "/post/post-edit";
        }
        postService.update(responseDTO);
        return "redirect:/posts/" + postId;
    }

    // ====== 게시글 삭제 ======
    @DeleteMapping("/{postId}")
    @VerifyPostOwner
    public String deletePost(@PathVariable("postId") Long postId) {
        postService.deleteById(postId);
        return "redirect:/posts";
    }

    // ====== 파일/이미지 다운로드 ======
    @ResponseBody
    @GetMapping("/uploads/images/{filename}")
    public Resource downloadImage(@PathVariable("filename") String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @ResponseBody
    @GetMapping("/uploads/files/{id}")
    public Resource downloadFile(@PathVariable("id") Long id,
                                 HttpServletResponse response) throws MalformedURLException {
        File file = fileService.getFile(id);
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(file.getStoredName()));
        String contentDisposition = buildContentDispositionHeader(file.getOriginalName());
        response.setHeader("Content-Disposition", contentDisposition);
        return resource;
    }

    // ====== 내부 변환/유틸 ======
    private void registerPostDetailModelAttributes(Model model, PostDetailResponseDTO postResponseDTO) {
        model.addAttribute("post", postResponseDTO);
        model.addAttribute("commentAddRequest", CommentToPostRequestDTO.builder().build());
        model.addAttribute("commentEditRequest", CommentToPostRequestDTO.builder().build());
    }

    // extractEmailFromPrincipal 메서드는 SecurityUtils 클래스로 이동됨

    private PostEditResponseDTO toPostEditResponseDTO(Post post) {
        return PostEditResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .attachments(post.getFiles())
                .build();
    }

    private String buildContentDispositionHeader(String originalFileName) {
        String encodedOriginFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        return "attachment; filename=\"" + encodedOriginFileName + "\"";
    }

    public static PostDetailResponseDTO getPostDetailResponseDTO(Post post, List<Comment> comments) {
        Map<Boolean, List<File>> imageFile = post.getFiles().stream()
                .collect(Collectors.partitioningBy(file -> file.getFileType().startsWith("image")));
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

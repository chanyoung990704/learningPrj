package org.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.VerifyPostOwner;
import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostCategoryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostCategoryService postCategoryService;
    private final CommentService commentService;
    private final FileService fileService;
    private final FileStore fileStore;

    @GetMapping("/{categoryName}")
    public String showPostListByCategory(
            @PathVariable String categoryName,
            @ModelAttribute("searchRequestDTO") PostSearchRequestDTO searchRequestDTO,
            @PageableDefault(sort = {"createdAt"}, size = PostRepository.DEFAULT_PAGE_SIZE,
                    direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        Long categoryId = postCategoryService.findByName(categoryName);
        searchRequestDTO.setCategoryId(categoryId);

        Page<PostListResponseDTO> postPage = postService.getPostList(searchRequestDTO, pageable);
        model.addAttribute("posts", postPage);
        model.addAttribute("categoryName", categoryName);

        return "post/post-list";
    }

    @GetMapping("/{categoryName}/{postId}")
    public String showPostDetail(@PathVariable("postId") Long postId,
                                 @PathVariable("categoryName") String categoryName,
                                 @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
                                 Model model) {
        PostDetailResponseDTO postResponseDTO = PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, commentPage);
        registerPostDetailModelAttributes(model, postResponseDTO);
        return "post/post-detail";
    }

    @GetMapping("/new")
    public String saveForm(@ModelAttribute("postRequestDTO") PostCreationRequestDTO postCreationRequestDTO) {
        return "post/post-form";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal Object principal,
                       @Validated @ModelAttribute("postRequestDTO") PostCreationRequestDTO postCreationRequestDTO,
                       BindingResult bindingResult,
                       HttpServletResponse response) throws IOException {
        if (bindingResult.hasErrors()) {
            return "post/post-form";
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        Long postId = postService.save(postCreationRequestDTO, email, postCreationRequestDTO.getCategoryId());
        PostCategory category = postCategoryService.findById(postCreationRequestDTO.getCategoryId());
        return new StringBuilder().append("redirect:/posts/").append(category.getName()).append("/").append(postId).toString();
    }

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
        PostCategory category = postCategoryService.findById(responseDTO.getCategoryId());
        postService.update(responseDTO, category);
        return new StringBuilder().append("redirect:/posts/").append(category.getName()).append("/").append(responseDTO.getId()).toString();
    }

    @DeleteMapping("/{postId}")
    @VerifyPostOwner
    public String deletePost(@PathVariable("postId") Long postId, @RequestParam("categoryName") String categoryName) {
        postService.deleteById(postId);
        return "redirect:/posts/" + categoryName;
    }

    @PostMapping("/{id}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {

        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        boolean isLiked = postService.toggleLike(id, email);
        Long likeCount = postService.getLikeCount(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        return response;
    }

    @ResponseBody
    @GetMapping("/uploads/images/{filename}")
    public Resource downloadImage(@PathVariable("filename") String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @GetMapping("/uploads/files/{id}")
    public Resource downloadFile(@PathVariable("id") Long fileId,
                                 HttpServletResponse response) throws MalformedURLException {
        File file = fileService.getFile(fileId);
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(file.getStoredName()));
        String contentDisposition = buildContentDispositionHeader(file.getOriginalName());
        response.setHeader("Content-Disposition", contentDisposition);
        return resource;
    }

    private void registerPostDetailModelAttributes(Model model, PostDetailResponseDTO postResponseDTO) {
        model.addAttribute("post", postResponseDTO);
        if (!model.containsAttribute("commentAddRequest")) {
            model.addAttribute("commentAddRequest", CommentCreationRequestDTO.builder().build());
        }
        if (!model.containsAttribute("replyAddRequest")) {
            model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
        }
        if (!model.containsAttribute("commentEditRequest")) {
            model.addAttribute("commentEditRequest", CommentCreationRequestDTO.builder().build());
        }
    }

    private PostEditResponseDTO toPostEditResponseDTO(Post post) {
        return PostEditResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .categoryId(post.getCategory().getId())
                .attachments(post.getFiles())
                .build();
    }

    private String buildContentDispositionHeader(String originalFileName) {
        String encodedOriginFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        return "attachment; filename=\"" + encodedOriginFileName + "\"";
    }
}

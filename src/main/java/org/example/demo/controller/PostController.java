package org.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.dto.response.CommentToPostResponseDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostCategoryService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostCategoryService postCategoryService;
    private final CommentService commentService;


    @ModelAttribute("categories")
    public List<PostCategory> getCategories() {
        return postCategoryService.findAll();
    }

    @GetMapping("/new")
    public String saveForm(@ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO) {
        return "/post/post-form";
    }

    @PostMapping("/new")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @Validated @ModelAttribute("postRequestDTO") PostRequestDTO postRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response) throws IOException {
        if(bindingResult.hasErrors()) {
            return "post/post-form";
        }

        postService.save(postRequestDTO, userDetails.getUsername(), postRequestDTO.getCategoryId());
        return "redirect:/posts";
    }

    @GetMapping
    public String showPostList(Model model) {
        // 게시글 가져오기
        List<Post> posts = postService.findPostsWithUserAndCategory();
        // Response 변환
        List<PostListResponseDTO> responseDTOS = posts.stream()
                .map(post -> new PostListResponseDTO(post.getId(), post.getTitle(), post.getContent(), post.getUser().getName(), post.getUpdatedAt(), post.getCategory()))
                .collect(Collectors.toList());

        model.addAttribute("posts", responseDTOS);
        return "post/post-list";

    }

    /**
     * 런타임 에러 언체크 예외
     */
    @GetMapping("/{id}")
    public String showPostDetail(@PathVariable Long id, Model model) {
        // 게시글 가져오기
        Post post = postService.findPostWithUserAndCategoryAndComments(id);
        // 게시글 댓글 가져오기
        List<Comment> comments = commentService.findCommentsByPostIdWithUserAndPost(post.getId());
        // Response DTO 변환
        PostDetailResponseDTO postResponseDTO = getPostDetailResponseDTO(post);
        List<CommentToPostResponseDTO> commentResponseDTO =
                comments.stream().map(comment -> new CommentToPostResponseDTO(comment.getContent(), comment.getUser().getName(),
                        comment.getUpdatedAt())).collect(Collectors.toList());

        model.addAttribute("post", postResponseDTO);
        model.addAttribute("comments", commentResponseDTO);
        model.addAttribute("commentToPostRequestDTO", new CommentToPostRequestDTO());
        return "post/post-detail";
    }

    private PostDetailResponseDTO getPostDetailResponseDTO(Post post) {
        PostDetailResponseDTO postDetailResponseDTO = new PostDetailResponseDTO();
        postDetailResponseDTO.setId(post.getId());
        postDetailResponseDTO.setTitle(post.getTitle());
        postDetailResponseDTO.setContent(post.getContent());
        postDetailResponseDTO.setTime(post.getUpdatedAt());
        postDetailResponseDTO.setCategory(post.getCategory());
        postDetailResponseDTO.setAuthor(post.getUser().getName());
        return postDetailResponseDTO;
    }
}

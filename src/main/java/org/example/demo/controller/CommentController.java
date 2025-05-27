package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.aop.VerifyCommentOwner;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.oauth2.CustomOAuth2User;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;

    // ====== 댓글 작성 ======
    /**
     * 게시글 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    public String addCommentToPost(@PathVariable("postId") Long id, @AuthenticationPrincipal Object principal, Model model,
                                   @Validated @ModelAttribute("commentAddRequest") CommentToPostRequestDTO commentAddDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            putPostDetail(id, model);
            registerCommentFormAttributes(model, false);
            return "post/post-detail";
        }
        String email = extractEmailFromPrincipal(principal);
        commentService.save(commentAddDTO, email, id);
        return "redirect:/posts/{postId}";
    }

    // ====== 댓글 삭제 ======
    /**
     * 게시글 댓글 삭제
     */
    @VerifyCommentOwner
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
        return "redirect:/posts/{postId}";
    }

    // ====== 댓글 수정 ======
    /**
     * 게시글 댓글 수정
     */
    @VerifyCommentOwner
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public String updateCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, Model model,
                                      @Validated @ModelAttribute("commentEditRequest") CommentToPostRequestDTO editDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            registerCommentFormAttributes(model, true);
            return "post/post-detail";
        }
        commentService.update(commentId, editDTO);
        return "redirect:/posts/" + postId;
    }

    // ====== 내부 유틸 ======
    /**
     * 게시글 ID의 내용 모델에 추가
     * @param postId
     * @param model
     */
    private void putPostDetail(Long postId, Model model) {
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);
        List<Comment> comments = commentService.findCommentsByPostIdWithUserAndPost(post.getId());
        PostDetailResponseDTO postResponseDTO = PostController.getPostDetailResponseDTO(post, comments);
        model.addAttribute("post", postResponseDTO);
    }

    /**
     * principal에서 이메일 추출
     */
    private String extractEmailFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getEmail();
        } else if (principal instanceof OAuth2User) {
            return (String) ((OAuth2User) principal).getAttribute("email");
        } else {
            throw new IllegalArgumentException("지원하지 않는 인증 타입입니다: " + principal.getClass().getName());
        }
    }

    /**
     * 댓글 작성/수정 폼 모델 등록
     * @param model
     * @param isEdit true면 댓글작성 폼, false면 댓글수정 폼을 추가
     */
    private void registerCommentFormAttributes(Model model, boolean isEdit) {
        if (isEdit) {
            model.addAttribute("commentAddRequest", CommentToPostRequestDTO.builder().build());
        } else {
            model.addAttribute("commentEditRequest", CommentToPostRequestDTO.builder().build());
        }
    }
}

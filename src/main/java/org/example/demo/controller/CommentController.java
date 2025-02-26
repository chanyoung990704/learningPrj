package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.aop.VerifyCommentOwner;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    /**
     * 게시글 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    public String addCommentToPost(@PathVariable("postId") Long id, @AuthenticationPrincipal UserDetails userDetails, Model model,
                                   @Validated @ModelAttribute("commentAddRequest") CommentToPostRequestDTO commentAddDTO, BindingResult bindingResult) {
        // 오류 확인
        if (bindingResult.hasErrors()) {
            // 모델에 게시글 정보 담기
            putPostDetail(id, model);
            // 댓글 수정을 위한 폼 모델
            model.addAttribute("commentEditRequest", CommentToPostRequestDTO.builder().build());

            return "post/post-detail";
        }
        commentService.save(commentAddDTO, userDetails.getUsername(), id);
        return "redirect:/posts/{postId}";
    }

    /**
     * 게시글 댓글 삭제
     */

    @VerifyCommentOwner
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        // 삭제
        commentService.deleteById(commentId);
        return "redirect:/posts/{postId}";
    }

    /**
     * 게시글 댓글 수정
     */

    @VerifyCommentOwner
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public String updateCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, Model model,
                                      @Validated @ModelAttribute("commentEditRequest") CommentToPostRequestDTO editDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            // 댓글 작성을 위한 폼 모델
            model.addAttribute("commentAddRequest", CommentToPostRequestDTO.builder().build());

            return "post/post-detail";
        }

        // 수정
        commentService.update(commentId, editDTO);

        return "redirect:/posts/" + postId;
    }

    /**
     * 게시글 ID의 내용 모델에 추가
     * @param postId
     * @param model
     */

    private void putPostDetail(Long postId, Model model) {
        // 게시글 가져오기
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);
        // 게시글 댓글 가져오기
        List<Comment> comments = commentService.findCommentsByPostIdWithUserAndPost(post.getId());
        // Response DTO 변환
        PostDetailResponseDTO postResponseDTO = PostController.getPostDetailResponseDTO(post, comments);

        model.addAttribute("post", postResponseDTO);
    }
}

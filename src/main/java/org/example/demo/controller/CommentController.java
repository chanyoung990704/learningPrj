package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.VerifyCommentOwner;
import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostCategoryService;
import org.example.demo.service.PostService;
import org.example.demo.util.PostDetailUtil;
import org.example.demo.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final PostCategoryService categoryService;

    @PostMapping("/posts/{postId}/comments")
    public String addCommentToPost(@PathVariable("postId") Long postId,
                                   @AuthenticationPrincipal Object principal,
                                   Model model,
                                   @Validated @ModelAttribute("commentAddRequest") CommentCreationRequestDTO commentAddDTO,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            registerCommentFormAttributes(model, false);
            return "post/post-detail";
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        commentService.save(commentAddDTO, email, postId);
        String categoryName = postService.findCategoryName(postId);
        return new StringBuilder().append("redirect:/posts/").append(categoryName).append("/").append(postId).toString();
    }

    @VerifyCommentOwner
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public String updateCommentToPost(@PathVariable("postId") Long postId,
                                      @PathVariable("commentId") Long commentId,
                                      Model model,
                                      @Validated @ModelAttribute("commentEditRequest") CommentCreationRequestDTO editDTO,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            registerCommentFormAttributes(model, true);
            return "post/post-detail";
        }
        commentService.update(commentId, editDTO);
        String categoryName = postService.findCategoryName(postId);
        return new StringBuilder().append("redirect:/posts/").append(categoryName).append("/").append(postId).toString();
    }

    @VerifyCommentOwner
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteCommentToPost(@PathVariable("postId") Long postId,
                                      @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
        String categoryName = postService.findCategoryName(postId);
        return new StringBuilder().append("redirect:/posts/").append(categoryName).append("/").append(postId).toString();
    }

    @PostMapping("/posts/{postId}/comments/{parentId}/replies")
    public String addReplyToComment(@PathVariable("postId") Long postId,
                                    @PathVariable("parentId") Long parentId,
                                    @AuthenticationPrincipal Object principal,
                                    @Validated @ModelAttribute("replyAddRequest") CommentCreationRequestDTO replyAddDTO,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            replyAddDTO.setContent("");
            putPostDetail(postId, model);
            model.addAttribute("replyAddRequest", replyAddDTO);
            return "post/post-detail";
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        commentService.saveReply(replyAddDTO, email, postId, parentId);
        String categoryName = postService.findCategoryName(postId);
        return new StringBuilder().append("redirect:/posts/").append(categoryName).append("/").append(postId).toString();
    }

    @VerifyCommentOwner
    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommentApi(@PathVariable("postId") Long postId,
                                                                @PathVariable("commentId") Long commentId,
                                                                @Validated @RequestBody CommentCreationRequestDTO editDTO,
                                                                BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "유효하지 않은 입력입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            commentService.update(commentId, editDTO);
            Comment updatedComment = commentService.findById(commentId);
            response.put("success", true);
            response.put("message", "댓글이 수정되었습니다.");
            response.put("content", updatedComment.getContent());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating comment via API: postId={}, commentId={}", postId, commentId, e);
            response.put("success", false);
            response.put("message", "댓글 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void putPostDetail(Long postId, Model model) {
        PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, 0);
    }

    private void registerCommentFormAttributes(Model model, boolean isEdit) {
        if (isEdit) {
            if (!model.containsAttribute("commentEditRequest")) {
                model.addAttribute("commentEditRequest", CommentCreationRequestDTO.builder().build());
            }
        } else {
            if (!model.containsAttribute("commentAddRequest")) {
                model.addAttribute("commentAddRequest", CommentCreationRequestDTO.builder().build());
            }
            if (!model.containsAttribute("replyAddRequest")) {
                model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
            }
        }
    }
}
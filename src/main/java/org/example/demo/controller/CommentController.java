package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.VerifyCommentOwner;
import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.service.CommentService;
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

    // ====== 댓글 작성 ======
    /**
     * 게시글 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    public String addCommentToPost(@PathVariable("postId") Long id, @AuthenticationPrincipal Object principal, Model model,
                                   @Validated @ModelAttribute("commentAddRequest") CommentCreationRequestDTO commentAddDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            putPostDetail(id, model);
            registerCommentFormAttributes(model, false);
            return "post/post-detail";
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal);
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
                                      @Validated @ModelAttribute("commentEditRequest") CommentCreationRequestDTO editDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            registerCommentFormAttributes(model, true);
            return "post/post-detail";
        }
        commentService.update(commentId, editDTO);
        return "redirect:/posts/" + postId;
    }

    // ====== 댓글 수정 (AJAX 요청 처리용) ======
    /**
     * 게시글 댓글 수정 (AJAX 요청 처리용)
     */
    @VerifyCommentOwner
    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommentApi(
            @PathVariable("postId") Long postId, 
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
            response.put("success", false);
            response.put("message", "댓글 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ====== 내부 유틸 ======
    /**
     * 게시글 ID의 내용 모델에 추가
     * @param postId
     * @param model
     */
    private void putPostDetail(Long postId, Model model) {
        // 댓글 추가/수정 실패 시 첫 페이지의 댓글을 표시
        PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, 0);
    }

    /**
     * 댓글 작성/수정 폼 모델 등록
     * @param model
     * @param isEdit true면 댓글작성 폼, false면 댓글수정 폼을 추가
     */
    /**
     * 댓글/답글 작성/수정 폼 모델 등록
     * @param model
     * @param isEdit true면 댓글수정 폼, false면 댓글/답글 작성 폼을 추가
     */
    private void registerCommentFormAttributes(Model model, boolean isEdit) {
        if (isEdit) {
            model.addAttribute("commentEditRequest", CommentCreationRequestDTO.builder().build());
        } else {
            model.addAttribute("commentAddRequest", CommentCreationRequestDTO.builder().build());
            model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
        }
    }

    /**
     * 게시글 답글 작성
     */
    @PostMapping("/posts/{postId}/comments/{parentId}/replies")
    public String addReplyToComment(
            @PathVariable("postId") Long postId,
            @PathVariable("parentId") Long parentId,
            @AuthenticationPrincipal Object principal,
            @Validated @ModelAttribute("replyAddRequest") CommentCreationRequestDTO replyAddDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            putPostDetail(postId, model);
            registerReplyFormAttributes(model, false);
            return "post/post-detail";
        }

        String email = SecurityUtils.extractEmailFromPrincipal(principal);
        commentService.saveReply(replyAddDTO, email, postId, parentId);
        return "redirect:/posts/" + postId;
    }

    // 댓글/답글 폼 모델 등록을 위한 유틸리티 메서드
    private void registerReplyFormAttributes(Model model, boolean isEdit) {
        if (isEdit) {
            model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
        } else {
            model.addAttribute("replyEditRequest", CommentCreationRequestDTO.builder().build());
        }
    }
}

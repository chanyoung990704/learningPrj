package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.VerifyCommentOwner; // 댓글 소유자 검증 AOP
import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.util.PostDetailUtil; // 게시글 상세 정보 모델 추가 유틸
import org.example.demo.util.SecurityUtils;  // 현재 사용자 정보 추출 유틸
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 현재 인증된 사용자 정보 주입
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // 유효성 검증 결과
import org.springframework.validation.annotation.Validated; // 유효성 검증 활성화
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j // Lombok 로깅 어노테이션
@Controller // 스프링 MVC 컨트롤러
@RequiredArgsConstructor // Lombok final 필드 생성자 자동 주입
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;

    // === 일반 댓글 관련 기능 (HTML 폼 처리) ===

    /**
     * (POST) 특정 게시글에 새로운 댓글을 작성합니다.
     */
    @PostMapping("/posts/{postId}/comments")
    public String addCommentToPost(@PathVariable("postId") Long postId, // 경로 변수: 게시글 ID
                                   @AuthenticationPrincipal Object principal, // 현재 인증 사용자
                                   Model model, // 뷰에 전달할 데이터
                                   @Validated @ModelAttribute("commentAddRequest") CommentCreationRequestDTO commentAddDTO, // 폼 데이터 바인딩 및 유효성 검증
                                   BindingResult bindingResult) { // 유효성 검증 결과
        if (bindingResult.hasErrors()) { // 유효성 검사 실패 시
            putPostDetail(postId, model); // 게시글 상세 정보 다시 로드
            registerCommentFormAttributes(model, false); // 댓글 작성 폼 모델 다시 등록
            return "post/post-detail"; // 동일 페이지로 리턴 (에러 메시지 표시)
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal); // 사용자 이메일 추출
        commentService.save(commentAddDTO, email, postId); // 댓글 저장 서비스 호출
        return "redirect:/posts/" + postId; // 해당 게시글 상세 페이지로 리다이렉트
    }

    /**
     * (PUT) 특정 게시글의 댓글을 수정합니다.
     */
    @VerifyCommentOwner // 댓글 소유자 검증 AOP 적용
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public String updateCommentToPost(@PathVariable("postId") Long postId, // 경로 변수: 게시글 ID
                                      @PathVariable("commentId") Long commentId, // 경로 변수: 댓글 ID
                                      Model model,
                                      @Validated @ModelAttribute("commentEditRequest") CommentCreationRequestDTO editDTO, // 폼 데이터 바인딩 및 유효성 검증
                                      BindingResult bindingResult) {
        if(bindingResult.hasErrors()) { // 유효성 검사 실패 시
            putPostDetail(postId, model); // 게시글 상세 정보 다시 로드
            registerCommentFormAttributes(model, true); // 댓글 수정 폼 모델 다시 등록
            return "post/post-detail"; // 동일 페이지로 리턴
        }
        commentService.update(commentId, editDTO); // 댓글 수정 서비스 호출
        return "redirect:/posts/" + postId; // 해당 게시글 상세 페이지로 리다이렉트
    }

    /**
     * (DELETE) 특정 게시글의 댓글을 삭제합니다.
     */
    @VerifyCommentOwner // 댓글 소유자 검증 AOP 적용
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteCommentToPost(@PathVariable("postId") Long postId, // 경로 변수: 게시글 ID
                                      @PathVariable("commentId") Long commentId) { // 경로 변수: 댓글 ID
        commentService.deleteById(commentId); // 댓글 삭제 서비스 호출
        return "redirect:/posts/" + postId; // 해당 게시글 상세 페이지로 리다이렉트
    }

    // === 답글 관련 기능 (HTML 폼 처리) ===

    /**
     * (POST) 특정 댓글에 새로운 답글을 작성합니다.
     */
    @PostMapping("/posts/{postId}/comments/{parentId}/replies")
    public String addReplyToComment(
            @PathVariable("postId") Long postId, // 경로 변수: 게시글 ID
            @PathVariable("parentId") Long parentId, // 경로 변수: 부모 댓글 ID
            @AuthenticationPrincipal Object principal,
            @Validated @ModelAttribute("replyAddRequest") CommentCreationRequestDTO replyAddDTO, // 폼 데이터 바인딩 및 유효성 검증
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) { // 유효성 검사 실패 시
            replyAddDTO.setContent(""); // 실패 시 입력 내용 초기화 (선택적)
            putPostDetail(postId, model); // 게시글 상세 정보 다시 로드
            model.addAttribute("replyAddRequest", replyAddDTO); // 유효성 검사 실패한 DTO를 다시 모델에 추가 (에러 메시지 표시용)
            return "post/post-detail"; // 동일 페이지로 리턴
        }

        String email = SecurityUtils.extractEmailFromPrincipal(principal); // 사용자 이메일 추출
        commentService.saveReply(replyAddDTO, email, postId, parentId); // 답글 저장 서비스 호출
        return "redirect:/posts/" + postId; // 해당 게시글 상세 페이지로 리다이렉트
    }


    // === API (AJAX) 댓글 수정 ===

    /**
     * (PUT) API: 특정 게시글의 댓글을 수정합니다 (AJAX 요청 처리용).
     */
    @VerifyCommentOwner // 댓글 소유자 검증 AOP 적용
    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    @ResponseBody // 응답 본문을 JSON 등으로 직접 반환
    public ResponseEntity<Map<String, Object>> updateCommentApi(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @Validated @RequestBody CommentCreationRequestDTO editDTO, // 요청 본문(JSON)을 DTO로 바인딩 및 유효성 검증
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>(); // API 응답용 Map 객체

        if (bindingResult.hasErrors()) { // 유효성 검사 실패 시
            response.put("success", false);
            response.put("message", "유효하지 않은 입력입니다.");
            return ResponseEntity.badRequest().body(response); // 400 Bad Request 응답
        }

        try {
            commentService.update(commentId, editDTO); // 댓글 수정 서비스 호출
            Comment updatedComment = commentService.findById(commentId); // 수정된 댓글 정보 조회

            response.put("success", true);
            response.put("message", "댓글이 수정되었습니다.");
            response.put("content", updatedComment.getContent()); // 수정된 내용 응답에 포함
            return ResponseEntity.ok(response); // 200 OK 응답

        } catch (Exception e) { // 예외 발생 시
            log.error("Error updating comment via API: postId={}, commentId={}", postId, commentId, e); // 에러 로그 기록
            response.put("success", false);
            response.put("message", "댓글 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Internal Server Error 응답
        }
    }


    // === 내부 유틸리티 메서드 ===

    /**
     * (private) 게시글 상세 정보를 조회하여 모델에 추가합니다 (댓글 관련 폼 에러 시 사용).
     */
    private void putPostDetail(Long postId, Model model) {
        // 댓글 추가/수정 실패 시 게시글 상세 정보와 첫 페이지의 댓글을 다시 로드하여 모델에 추가
        PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, 0);
    }

    /**
     * (private) 댓글/답글 작성 및 수정 폼을 위한 빈 DTO 객체를 모델에 등록합니다.
     */
    private void registerCommentFormAttributes(Model model, boolean isEdit) {
        if (isEdit) { // 수정 폼인 경우
            if (!model.containsAttribute("commentEditRequest")) { // 모델에 이미 없다면 새로 추가 (유효성 검사 실패로 다시 돌아온 경우 유지)
                model.addAttribute("commentEditRequest", CommentCreationRequestDTO.builder().build());
            }
        } else { // 작성 폼인 경우
            if (!model.containsAttribute("commentAddRequest")) {
                model.addAttribute("commentAddRequest", CommentCreationRequestDTO.builder().build());
            }
            // 답글 작성 폼도 기본적으로 함께 제공 (replyAddRequest 이름의 DTO)
            // addReplyToComment 메서드에서 @ModelAttribute("replyAddRequest")로 바인딩 되므로, 여기서도 동일 이름 사용
            if (!model.containsAttribute("replyAddRequest")) {
                model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
            }
        }
    }

    // `registerReplyFormAttributes` 메서드는 `registerCommentFormAttributes`와 기능이 중복되고,
    // 실제 답글 작성/수정 로직에서 사용되는 모델 어트리뷰트 이름(`replyAddRequest`, `replyEditRequest`)과
    // `registerCommentFormAttributes`에서 `replyAddRequest`를 이미 처리하고 있으므로,
    // 혼란을 줄이기 위해 제거하거나 `registerCommentFormAttributes`에 통합하는 것을 고려할 수 있습니다.
    // 여기서는 일단 주석 처리합니다.
    /*
    private void registerReplyFormAttributes(Model model, boolean isEdit) {
        if (isEdit) {
            model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build()); // 수정 시에는 replyEditRequest가 더 적절할 수 있음
        } else {
            model.addAttribute("replyEditRequest", CommentCreationRequestDTO.builder().build()); // 작성 시에는 replyAddRequest가 더 적절
        }
    }
    */
}

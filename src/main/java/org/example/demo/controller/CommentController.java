package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 게시글 댓글 작성
     */
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addCommentToPost(@PathVariable("postId") Long id, @AuthenticationPrincipal UserDetails userDetails,
                                   @Validated @ModelAttribute("commentToPostRequestDTO") CommentToPostRequestDTO requestDTO, BindingResult bindingResult){
        // 오류 확인
        if (bindingResult.hasErrors()) {
            return "/post/post-detail";
        }

        commentService.save(requestDTO, userDetails.getUsername(), id);

        return "redirect:/posts/{postId}";
    }
}

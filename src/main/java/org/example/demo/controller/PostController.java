package org.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.service.PostCategoryService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostCategoryService postCategoryService;
    private final UserService userService;


    @ModelAttribute("categories")
    public List<PostCategory> getCategories() {
        return postCategoryService.findAll();
    }

    @GetMapping("/save")
    public String saveForm(@ModelAttribute PostRequestDTO postRequestDTO) {
        return "post-form";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @Validated @ModelAttribute PostRequestDTO postRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response) throws IOException {
        if(bindingResult.hasErrors()) {
            return "post-form";
        }

        // 사용자 정보 가져오기
        User user = userService.findByEmail(userDetails.getUsername());
        /**
         * 카테고리 가져오기
         * 카테고리가 없는 경우 런타임 에러
         */
        PostCategory category = postCategoryService.findById(postRequestDTO.getCategoryId());

        // DTO를 Post로 변환
        Post post = PostRequestDTO.toPost(postRequestDTO, user, category);
        postService.save(post);

        return "redirect:/";
    }
}

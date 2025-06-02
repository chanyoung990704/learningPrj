package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.PostCategory;
import org.example.demo.dto.request.PostCategoryRequestDTO;
import org.example.demo.service.PostCategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/postcategories")
@RequiredArgsConstructor
public class PostCategoryController {

    private final PostCategoryService postCategoryService;

    // 카테고리 등록 폼 제공
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("requestDTO", PostCategoryRequestDTO.builder().build());
        return "post/createPostCategoryForm";
    }

    // 카테고리 등록 처리
    @PostMapping("/new")
    @CacheEvict(value = "categoriesCache", allEntries = true) // 캐시 삭제
    public String create(@Validated @ModelAttribute("requestDTO") PostCategoryRequestDTO requestDTO, BindingResult result) {
        // 오류 존재 시
        if (result.hasErrors()) {
            return "post/createPostCategoryForm";
        }

        try {
            postCategoryService.createCategory(requestDTO);
        } catch (IllegalStateException e) {
            // 중복 카테고리 오류 처리
            result.rejectValue("name", "error.duplicatedCategory", e.getMessage());
            return "post/createPostCategoryForm";
        }

        return "redirect:/postcategories/new";
    }

}

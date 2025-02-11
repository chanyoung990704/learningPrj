package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.PostCategory;
import org.example.demo.dto.request.PostCategoryRequestDTO;
import org.example.demo.service.PostCategoryService;
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

    @ModelAttribute("categories")
    public List<PostCategory> getCategories() {
        return postCategoryService.findAll();
    }


    // 카테고리 등록 폼 제공
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("requestDTO", new PostCategoryRequestDTO());
        return "createPostCategoryForm";
    }

    // 카테고리 등록 처리
    @PostMapping("/new")
    public String create(@Validated @ModelAttribute("requestDTO") PostCategoryRequestDTO requestDTO, BindingResult result) {
        // 오류 존재 시
        if (result.hasErrors()) {
            return "createPostCategoryForm";
        }

        PostCategory newCategory = getPostCategory(requestDTO);

        postCategoryService.save(newCategory);

        return "redirect:/postcategories/new";
    }

    private PostCategory getPostCategory(PostCategoryRequestDTO requestDTO) {
        // 부모 카테고리 설정
        PostCategory parent = getParentCategory(requestDTO.getParentId());

        // 이미 중복된 카테고리인지 확인
        if (isDuplicatedCategory(parent, requestDTO.getName())) {
            throw new IllegalStateException("이미 " + requestDTO.getName() + " 카테고리가 존재합니다");
        }

        // 새로운 카테고리 생성 및 저장
        PostCategory newCategory = PostCategory.builder()
                .name(requestDTO.getName())
                .build();

        if (parent != null) {
            parent.addChild(newCategory);
        }
        return newCategory;
    }

    private PostCategory getParentCategory(Long parentId) {
        if (parentId == null) {
            return null;
        }
        // parentID가 존재하지 않는 경우 런타임 에러
        return postCategoryService.findById(parentId);
    }

    private boolean isDuplicatedCategory(PostCategory parent, String categoryName) {
        if (parent == null) {
            return false; // 부모 카테고리가 없으면 중복 확인 필요 없음
        }
        return parent.getChildren()
                .stream().map(PostCategory::getName)
                .anyMatch(name -> name.equals(categoryName));
    }

}

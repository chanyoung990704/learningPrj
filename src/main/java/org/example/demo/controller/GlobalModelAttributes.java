package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.PostCategory;
import org.example.demo.service.PostCategoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final PostCategoryService postCategoryService;

    @Cacheable("categoriesCache")
    @ModelAttribute("allCategories")
    public List<PostCategory> getCategories() {
        return postCategoryService.findAll();
    }

}

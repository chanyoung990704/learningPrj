package org.example.demo.service;

import org.example.demo.domain.PostCategory;
import org.example.demo.dto.request.PostCategoryRequestDTO;

public interface PostCategoryService extends BaseService<PostCategory> {

    PostCategory findByName(String name);

    void createCategory(PostCategoryRequestDTO requestDTO);
}

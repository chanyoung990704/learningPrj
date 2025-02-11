package org.example.demo.service;

import org.example.demo.domain.PostCategory;

public interface PostCategoryService extends BaseService<PostCategory> {

    PostCategory findByName(String name);
}

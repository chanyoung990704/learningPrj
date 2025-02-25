package org.example.demo.service;

import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostService extends BaseService<Post> {
    @Transactional
    Long save(PostRequestDTO requestDTO, String email, Long categoryId);

    List<Post> findPostsWithUser();

    Post findPostWithUser(Long id);

    List<Post> findPostsWithUserAndCategory();

    Post findPostWithUserAndCategoryAndComments(Long id);
}

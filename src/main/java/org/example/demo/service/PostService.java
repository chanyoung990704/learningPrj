package org.example.demo.service;

import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostService extends BaseService<Post> {
    @Transactional
    Long save(PostCreationRequestDTO requestDTO, String email, Long categoryId);

    List<Post> findPostsWithUser();

    Page<Post> findPostsWithUserAndCategory(Pageable pageable);

    Post findPostWithUser(Long id);

    List<Post> findPostsWithUserAndCategory();

    Post findPostWithUserAndCategory(Long id);

    Post findPostWithUserAndCategoryAndFiles(Long id);

    Long update(PostEditResponseDTO responseDTO);

    Page<Post> searchPosts(PostSearchRequestDTO requestDTO, Pageable pageable);

    Page<PostListResponseDTO> searchPostsV2(PostSearchRequestDTO requestDTO, Pageable pageable);

    Page<PostListResponseDTO> getPostList(PostSearchRequestDTO requestDTO, Pageable pageable);

}

package org.example.demo.repository.querydsl;

import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QueryDslPostRepository {
    Page<Post> findPostsBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO);

    Page<PostListResponseDTO> findPostsBySearchWithUserAndCategoryV2(Pageable pageable, PostSearchRequestDTO requestDTO);

    /**
     * 게시글 목록을 조회합니다. (PostListResponseDTO 반환)
     * @param pageable 페이징 정보
     * @param requestDTO 검색 조건
     * @return PostListResponseDTO 페이지
     */
    Page<PostListResponseDTO> findPostsListBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO);

}

package org.example.demo.repository.querydsl;

import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QueryDslPostRepository {
    /**
     * 게시글 목록을 조회합니다. (PostListResponseDTO 반환)
     * @param pageable 페이징 정보
     * @param requestDTO 검색 조건
     * @return PostListResponseDTO 페이지
     */
    Page<PostListResponseDTO> findPostsListBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO);

    // 카테고리 조건 추가
    Page<PostListResponseDTO> findPostsListBySearchWithUserAndCategoryV2(Pageable pageable, PostSearchRequestDTO requestDTO);
}

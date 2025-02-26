package org.example.demo.repository.querydsl;

import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QueryDslPostRepository {
    Page<Post> findPostsBySearchWithUserAndCategory(Pageable pageable, PostSearchRequestDTO requestDTO);
}

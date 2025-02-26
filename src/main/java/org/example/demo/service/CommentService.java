package org.example.demo.service;

import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentService extends BaseService<Comment> {
    @Transactional
    Long save(CommentToPostRequestDTO requestDTO, String email, Long postId);

    List<Comment> findCommentsByPostIdWithUserAndPost(Long postId);

    Comment findCommentByIdWithUser(Long commentId);

    @Transactional
    Long update(Long id, CommentToPostRequestDTO updateDto);
}

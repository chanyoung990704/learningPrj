package org.example.demo.service;

import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.response.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentService extends BaseService<Comment> {
    @Transactional
    Long save(CommentCreationRequestDTO requestDTO, String email, Long postId);

    @Transactional
    Long saveReply(CommentCreationRequestDTO requestDTO, String email, Long postId, Long parentId);

    List<Comment> findCommentsByPostIdWithUserAndPost(Long postId);

    Comment findCommentByIdWithUser(Long commentId);

    @Transactional
    Long update(Long id, CommentCreationRequestDTO updateDto);

    Page<CommentResponseDTO> getCommentsWithReplies(Long postId, Pageable pageable);
}

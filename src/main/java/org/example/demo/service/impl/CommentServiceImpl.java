package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.response.CommentResponseDTO;
import org.example.demo.repository.CommentRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    @Override
    @Transactional
    public Long save(Comment comment) {
        return commentRepository.save(comment).getId();
    }

    /**
     * 새로운 댓글을 저장합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO
     * @param email     작성자 이메일
     * @param postId    게시글 ID
     * @return 저장된 댓글 ID
     */
    @Override
    @Transactional
    public Long save(CommentCreationRequestDTO requestDTO, String email, Long postId) {
        Post post = postService.findById(postId);
        User user = userService.findByEmail(email);

        Comment comment = Comment.builder()
                .content(requestDTO.getContent())
                .user(user)
                .build();

        post.addComment(comment);
        return save(comment);
    }

    /**
     * 대댓글을 저장합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO
     * @param email     작성자 이메일
     * @param postId    게시글 ID
     * @param parentId  부모 댓글 ID
     * @return 저장된 대댓글 ID
     * @throws RuntimeException 부모 댓글이 존재하지 않거나 게시글 ID가 일치하지 않는 경우
     */
    @Override
    @Transactional
    public Long saveReply(CommentCreationRequestDTO requestDTO, String email, Long postId, Long parentId) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentId));

        // 부모 댓글이 속한 게시글과 요청된 게시글이 일치하는지 검증
        if (!parentComment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Post ID does not match the parent comment's post");
        }

        User user = userService.findByEmail(email);
        Comment reply = Comment.builder()
                .content(requestDTO.getContent())
                .user(user)
                .parent(parentComment)
                .build();

        parentComment.getPost().addComment(reply);
        return save(reply);
    }

    /**
     * ID로 댓글을 조회합니다.
     *
     * @param id 댓글 ID
     * @return 조회된 댓글 엔티티
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Comment with id " + id + " not found"));
    }

    @Override
    public List<Comment> findAll() {
        return List.of();
    }

    /**
     * 게시글 ID로 댓글 목록을 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    @Override
    public List<Comment> findCommentsByPostIdWithUserAndPost(Long postId) {
        return commentRepository.findCommentsByPostIdWithUserAndPost(postId);
    }

    /**
     * 게시글 ID로 댓글 목록을 페이징하여 조회합니다.
     *
     * @param postId   게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 목록
     */


    /**
     * 게시글 ID로 댓글 DTO 목록을 페이징하여 조회합니다.
     *
     * @param postId   게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 DTO 목록
     */

    /**
     * ID로 댓글을 사용자 정보와 함께 조회합니다.
     *
     * @param commentId 댓글 ID
     * @return 조회된 댓글 엔티티
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    public Comment findCommentByIdWithUser(Long commentId) {
        return commentRepository.findCommentByIdWithUser(commentId).orElseThrow(() ->
                new RuntimeException("Comment with id " + commentId + " not found"));
    }

    /**
     * 댓글을 삭제합니다.
     *
     * @param id 삭제할 댓글 ID
     * @return 삭제된 댓글 ID
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    @Transactional
    public Long deleteById(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment with id " + id + " not found");
        }
        commentRepository.deleteById(id);
        return id;
    }

    /**
     * 댓글을 업데이트합니다.
     *
     * @param id      업데이트할 댓글 ID
     * @param comment 업데이트할 댓글 정보를 가진 엔티티
     * @return 업데이트된 댓글 ID
     */
    @Override
    @Transactional
    public Long update(Long id, Comment comment) {
        Comment existingComment = findById(id);
        if (comment.getContent() != null) {
            existingComment.setContent(comment.getContent());
        }
        return existingComment.getId();
    }

    /**
     * 댓글 내용을 업데이트합니다.
     *
     * @param id       업데이트할 댓글 ID
     * @param updateDto 업데이트할 댓글 내용을 가진 DTO
     * @return 업데이트된 댓글 ID
     */
    @Override
    @Transactional
    public Long update(Long id, CommentCreationRequestDTO updateDto) {
        Comment existingComment = findById(id);
        if (updateDto != null && updateDto.getContent() != null) {
            existingComment.setContent(updateDto.getContent());
        }
        return existingComment.getId();
    }

    @Override
    public Page<CommentResponseDTO> getCommentsWithReplies(Long postId, Pageable pageable) {
        // 1. 루트 댓글(부모가 없는 댓글) 페이징 조회
        Page<Comment> rootComments = commentRepository.findCommentsByPostIdAndParentIsNull(postId, pageable);

        // 2. 각 댓글을 DTO로 변환 (자식 댓글까지 재귀적으로 변환)
        Page<CommentResponseDTO> dtoPage = rootComments.map(CommentResponseDTO::toDtoWithChildren);

        return dtoPage;
    }

}

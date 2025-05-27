package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentToPostRequestDTO;
import org.example.demo.repository.CommentRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;


    @Override
    @Transactional
    public Long save(Comment object) {
        return commentRepository.save(object).getId();
    }

    @Transactional
    @Override
    public Long save(CommentToPostRequestDTO requestDTO, String email, Long postId) {
        Post post = postService.findById(postId);
        User user = userService.findByEmail(email);
        // 댓글 생성
        Comment comment = Comment.builder()
                .content(requestDTO.getContent())
                .user(user).build();

        // 연관관계 설정
        post.addComment(comment);

        // 저장
        return commentRepository.save(comment).getId();
    }

    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Comment with id " + id + " not found"));
    }

    @Override
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Override
    public List<Comment> findCommentsByPostIdWithUserAndPost(Long postId){
        return commentRepository.findCommentsByPostIdWithUserAndPost(postId);
    }

    @Override
    public Page<Comment> findCommentsByPostIdWithUserAndPost(Long postId, Pageable pageable) {
        return commentRepository.findCommentsByPostIdWithUserAndPost(postId, pageable);
    }

    @Override
    public Comment findCommentByIdWithUser(Long commentId){
        return commentRepository.findCommentByIdWithUser(commentId).orElseThrow(() ->
                new RuntimeException("Comment with id " + commentId + " not found"));
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        if(!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment with id " + id + " not found");
        }
        commentRepository.deleteById(id);
        return id;
    }

    @Override
    @Transactional
    public Long update(Long id, Comment object) {
        Comment existingComment = findById(id);
        if(object.getContent() != null) {
            existingComment.setContent(object.getContent());
        }
        return existingComment.getId();    }

    @Transactional
    @Override
    public Long update(Long id, CommentToPostRequestDTO updateDto) {
        Comment existingComment = findById(id);
        if(updateDto != null && updateDto.getContent() != null) {
            existingComment.setContent(updateDto.getContent());
        }
        return existingComment.getId();
    }
}

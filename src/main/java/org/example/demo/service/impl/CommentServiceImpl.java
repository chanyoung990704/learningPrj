package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.repository.CommentRepository;
import org.example.demo.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;


    @Override
    @Transactional
    public Long save(Comment object) {
        return commentRepository.save(object).getId();
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
    public Long update(Long id, Comment updatedComment) {
        Comment existingComment = findById(id);
        if(updatedComment.getContent() != null) {
            existingComment.setContent(updatedComment.getContent());
        }
        return existingComment.getId();
    }
}

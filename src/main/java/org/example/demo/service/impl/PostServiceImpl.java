package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Post;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional
    public Long save(Post object) {
        return postRepository.save(object).getId();
    }

    @Override
    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        if(!postRepository.existsById(id)) {
            throw new RuntimeException("Post with id " + id + " not found");
        }
        postRepository.deleteById(id);
        return id;
    }

    @Override
    @Transactional
    public Long update(Long id, Post updatedPost) {
        Post existingPost = findById(id);
        updatePostDetails(existingPost, updatedPost);
        return existingPost.getId();
    }

    private void updatePostDetails(Post existingPost, Post updatedPost) {
        if (updatedPost.getTitle() != null) {
            existingPost.setTitle(updatedPost.getTitle());
        }
        if(updatedPost.getContent() != null) {
            existingPost.setContent(updatedPost.getContent());
        }
        if(updatedPost.getCategory() != null) {
            existingPost.setCategory(updatedPost.getCategory());
        }
        if(updatedPost.getFiles() != null) {
            existingPost.setFiles(updatedPost.getFiles());
        }

    }

}

package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostCategoryService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCategoryService postCategoryService;

    @Override
    @Transactional
    public Long save(Post object) {
        return postRepository.save(object).getId();
    }

    @Transactional
    @Override
    public Long save(PostRequestDTO requestDTO, String email, Long categoryId) {
        User user = userService.findByEmail(email);
        PostCategory category = postCategoryService.findById(categoryId);
        Post post = PostRequestDTO.toPost(requestDTO, user, category);

        return postRepository.save(post).getId();
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
    public List<Post> findPostsWithUser() {
        return postRepository.findPostsWithUser();
    }

    @Override
    public Post findPostWithUser(Long id){
        return postRepository.findPostWithUser(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public List<Post> findPostsWithUserAndCategory() {
        return postRepository.findPostsWithUserAndCategory();
    }

    @Override
    public Post findPostWithUserAndCategoryAndComments(Long id){
        Post post = findPostWithUser(id);
        // 지연로딩
        post.getCategory().getName();

        return post;
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

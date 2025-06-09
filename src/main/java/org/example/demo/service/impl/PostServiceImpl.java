package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.PostLike;
import org.example.demo.domain.User;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.repository.PostLikeRepository;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.PostCategoryService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCategoryService postCategoryService;
    private final FileService fileService;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    @Override
    public Long save(PostCreationRequestDTO requestDTO, String email, Long categoryId) {
        User user = userService.findByEmail(email);
        PostCategory category = postCategoryService.findById(categoryId);
        Post post = PostCreationRequestDTO.toPost(requestDTO, user, category);
        uploadFiles(requestDTO.getFiles(), post);
        return postRepository.save(post).getId();
    }

    @Override
    @Transactional
    public Long save(Post post) {
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
    public Post findPostWithUser(Long id) {
        return postRepository.findPostWithUser(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public Post findPostWithUserAndCategoryAndFiles(Long id) {
        return postRepository.findByIdWithUserAndCategoryAndFiles(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public Page<PostListResponseDTO> getPostList(PostSearchRequestDTO requestDTO, Pageable pageable) {
        Page<PostListResponseDTO> result = postRepository.findPostsListBySearchWithUserAndCategoryV2(pageable, requestDTO);
        result.getContent().forEach(dto -> {
            long count = postLikeRepository.countByPostId(dto.getId());
            dto.setLikeCount(count);
        });
        return result;
    }

    @Transactional
    @Override
    public Post getPostDetailsAndIncreaseViews(Long postId) {
        increaseViews(postId);
        return findPostWithUserAndCategoryAndFiles(postId);
    }

    @Transactional
    @Override
    public Long update(PostEditResponseDTO responseDTO, PostCategory category) {
        Post post = findById(responseDTO.getId());
        updatePostDetails(post, responseDTO, category);
        return post.getId();
    }

    @Override
    @Deprecated
    public Long update(Long id, Post object) {
        return 0L;
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post with id " + id + " not found");
        }
        postRepository.deleteById(id);
        return id;
    }

    @Transactional
    @Override
    public boolean toggleLike(Long postId, String email) {
        Post post = findById(postId);
        User user = userService.findByEmail(email);
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            post.removeLike(existingLike.get());
            postLikeRepository.delete(existingLike.get());
            return false;
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            post.addLike(newLike);
            postLikeRepository.save(newLike);
            return true;
        }
    }

    @Override
    public long getLikeCount(Long postId) {
        Post post = findById(postId);
        return postLikeRepository.countByPost(post);
    }

    @Transactional
    public void increaseViews(Long postId) {
        postRepository.updateViewCount(postId);
    }

    private void updatePostDetails(Post existingPost, PostEditResponseDTO responseDTO, PostCategory category) {
        if (responseDTO.getTitle() != null) {
            existingPost.setTitle(responseDTO.getTitle());
        }
        if (responseDTO.getContent() != null) {
            existingPost.setContent(responseDTO.getContent());
        }
        if (category != null) {
            existingPost.setCategory(category);
        }

        if (responseDTO.getDeletedAttachmentsId() != null && !responseDTO.getDeletedAttachmentsId().isEmpty()) {
            responseDTO.getDeletedAttachmentsId().forEach(fileId -> {
                existingPost.removeAttachment(fileService.getFile(fileId));
            });
        }

        if (responseDTO.getNewAttachments() != null && !responseDTO.getNewAttachments().isEmpty()) {
            uploadFiles(responseDTO.getNewAttachments(), existingPost);
        }
    }

    private void uploadFiles(List<MultipartFile> files, Post post) {
        if (files != null && !files.isEmpty()) {
            fileService.addFileToPost(files, post);
        }
    }

    @Override
    public String findCategoryName(Long id) {
        return postRepository.findCategoryNameById(id).orElseThrow(
                () -> new RuntimeException("게시글 없음")
        );
    }
}
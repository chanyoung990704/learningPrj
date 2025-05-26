package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.domain.PostCategory;
import org.example.demo.domain.User;
import org.example.demo.dto.request.PostRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCategoryService postCategoryService;
    private final FileService fileService;

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

        // 파일 업로드 처리
        uploadFiles(requestDTO.getFiles(), post);

        // Post는 File에 대한 CASCADE.ALL
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
    public List<Post> findPostsWithUserAndCategory() {
        return postRepository.findPostsWithUserAndCategory();
    }

    @Override
    public Page<Post> findPostsWithUserAndCategory(Pageable pageable){
        return postRepository.findPostsWithUserAndCategory(pageable);
    }

    @Override
    public Post findPostWithUser(Long id){
        return postRepository.findPostWithUser(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public Post findPostWithUserAndCategory(Long id){
        return postRepository.findByIdWithUserAndCategory(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    @Override
    public Post findPostWithUserAndCategoryAndFiles(Long id){
        return postRepository.findByIdWithUserAndCategoryAndFiles(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
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
    @Deprecated
    public Long update(Long id, Post object) {
        return 0L;
    }

    @Override
    @Transactional
    public Long update(PostEditResponseDTO responseDTO) {
        // 영속성 컨텍스트에 불러오기
        Post post = findById(responseDTO.getId());

        // 더티체킹
        updatePostDetails(post, responseDTO);

        return post.getId();
    }

    @Override
    public Page<Post> searchPosts(PostSearchRequestDTO searchRequestDTO, Pageable pageable) {
        return postRepository.findPostsBySearchWithUserAndCategory(pageable, searchRequestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponseDTO> getPostList(PostSearchRequestDTO requestDTO, Pageable pageable) {
        Page<Post> postPage = postRepository.findPostsBySearchWithUserAndCategory(pageable, requestDTO);
        return postPage.map(post -> new PostListResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getUser() != null ? post.getUser().getName() : null, // Handle potential null user
                post.getUpdatedAt(), // Assuming 'time' in DTO maps to updatedAt
                post.getCategory()
        ));
    }

    @Override
    public Page<PostListResponseDTO> searchPostsV2(PostSearchRequestDTO requestDTO, Pageable pageable) {
        return postRepository.findPostsBySearchWithUserAndCategoryV2(pageable, requestDTO);
    }


    private void updatePostDetails(Post existingPost, PostEditResponseDTO responseDTO) {
        if (responseDTO.getTitle() != null) {
            existingPost.setTitle(responseDTO.getTitle());
        }
        if(responseDTO.getContent() != null) {
            existingPost.setContent(responseDTO.getContent());
        }
        if(responseDTO.getCategory() != null) {
            existingPost.setCategory(responseDTO.getCategory());
        }

        // 첨부된 파일 삭제
        if(responseDTO.getDeletedAttachmentsId() != null && !responseDTO.getDeletedAttachmentsId().isEmpty()){
            responseDTO.getDeletedAttachmentsId().forEach(id -> {
                existingPost.removeAttachment(fileService.getFile(id));
            });
        }

        // 새로운 파일 업로드
        if(responseDTO.getNewAttachments() != null && !responseDTO.getNewAttachments().isEmpty()){
            uploadFiles(responseDTO.getNewAttachments(), existingPost);
        }

    }

    private void uploadFiles(List<MultipartFile> files, Post post) {
        if(!files.isEmpty()){
            // 연관관계 세팅 이루어짐
            fileService.addFileToPost(files, post);
        }
    }

}

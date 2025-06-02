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

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스 구현 클래스
 */
@Service
@Transactional(readOnly = true) // 클래스 레벨에서 기본적으로 읽기 전용 트랜잭션 적용
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCategoryService postCategoryService;
    private final FileService fileService; // FileService 주입
    private final PostLikeRepository postLikeRepository;

    // --- 게시글 생성 (Create) ---

    /**
     * PostCreationRequestDTO를 사용하여 새로운 게시글을 저장합니다.
     * 파일 업로드 처리를 포함합니다.
     *
     * @param requestDTO 게시글 생성 요청 DTO
     * @param email      작성자 이메일
     * @param categoryId 카테고리 ID
     * @return 저장된 게시글의 ID
     */
    @Transactional // 쓰기 작업이므로 클래스 레벨 트랜잭션 오버라이드
    @Override
    public Long save(PostCreationRequestDTO requestDTO, String email, Long categoryId) {
        User user = userService.findByEmail(email);
        PostCategory category = postCategoryService.findById(categoryId);
        Post post = PostCreationRequestDTO.toPost(requestDTO, user, category);

        // 첨부 파일 업로드 처리
        uploadFiles(requestDTO.getFiles(), post);

        // Post 엔티티는 File에 대해 CascadeType.ALL로 설정되어 있다고 가정, Post 저장 시 File도 함께 저장됨
        return postRepository.save(post).getId();
    }

    /**
     * Post 엔티티 객체를 직접 저장합니다. (BaseService의 메서드 구현)
     *
     * @param post 저장할 Post 엔티티
     * @return 저장된 게시글의 ID
     */
    @Override
    @Transactional // 쓰기 작업
    public Long save(Post post) {
        return postRepository.save(post).getId();
    }


    // --- 게시글 조회 (Read) ---

    /**
     * ID로 특정 게시글을 조회합니다. (BaseService의 메서드 구현)
     * 게시글이 없으면 RuntimeException을 발생시킵니다.
     *
     * @param id 조회할 게시글 ID
     * @return 조회된 Post 엔티티
     * @throws RuntimeException 게시글을 찾을 수 없을 때
     */
    @Override
    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    /**
     * 모든 게시글 목록을 조회합니다. (BaseService의 메서드 구현)
     *
     * @return 모든 Post 엔티티 목록
     */
    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    /**
     * ID로 특정 게시글을 조회하며, 작성자(User) 정보를 함께 Fetch Join 합니다.
     * 게시글이 없으면 RuntimeException을 발생시킵니다.
     *
     * @param id 조회할 게시글 ID
     * @return 조회된 Post 엔티티 (작성자 정보 포함)
     * @throws RuntimeException 게시글을 찾을 수 없을 때
     */
    @Override
    public Post findPostWithUser(Long id){
        return postRepository.findPostWithUser(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    /**
     * ID로 특정 게시글을 조회하며, 작성자(User), 카테고리(Category), 파일(Files) 정보를 함께 Fetch Join 합니다.
     * 게시글이 없으면 RuntimeException을 발생시킵니다.
     *
     * @param id 조회할 게시글 ID
     * @return 조회된 Post 엔티티 (작성자, 카테고리, 파일 정보 포함)
     * @throws RuntimeException 게시글을 찾을 수 없을 때
     */
    @Override
    public Post findPostWithUserAndCategoryAndFiles(Long id){
        return postRepository.findByIdWithUserAndCategoryAndFiles(id).orElseThrow(() ->
                new RuntimeException("Post with id " + id + " not found"));
    }

    /**
     * 검색 조건과 페이징 정보를 기반으로 게시글 목록(PostListResponseDTO)을 조회합니다.
     * 각 게시글의 좋아요 수를 포함하여 반환합니다.
     *
     * @param requestDTO 검색 조건 DTO
     * @param pageable   페이징 정보
     * @return 페이징된 게시글 목록 DTO (좋아요 수 포함)
     */
    @Override
    public Page<PostListResponseDTO> getPostList(PostSearchRequestDTO requestDTO, Pageable pageable) {
        // QueryDSL 등을 통해 검색 조건에 맞는 게시글 목록 조회 (사용자, 카테고리 정보 포함)
        Page<PostListResponseDTO> result = postRepository.findPostsListBySearchWithUserAndCategory(pageable, requestDTO);

        // 각 게시글 DTO에 좋아요 수 추가
        result.getContent().forEach(dto -> {
            long count = postLikeRepository.countByPostId(dto.getId());
            dto.setLikeCount(count);
        });

        return result;
    }

    /**
     * 특정 게시글의 상세 정보를 조회하고 조회수를 1 증가시킵니다.
     *
     * @param postId 조회할 게시글 ID
     * @return 조회수 증가 처리 후의 Post 엔티티 (작성자, 카테고리, 파일 정보 포함)
     */
    @Transactional // 조회수 업데이트(쓰기)와 조회(읽기)를 한 트랜잭션으로 묶음
    @Override
    public Post getPostDetailsAndIncreaseViews(Long postId) {
        increaseViews(postId); // 조회수 증가
        return findPostWithUserAndCategoryAndFiles(postId); // 상세 정보 조회
    }


    // --- 게시글 수정 (Update) ---

    /**
     * PostEditResponseDTO (요청 DTO로 사용)를 사용하여 기존 게시글 정보를 수정합니다.
     * 영속성 컨텍스트의 더티 체킹(Dirty Checking)을 활용하여 업데이트합니다.
     * 파일 추가 및 삭제 처리를 포함합니다.
     *
     * @param responseDTO 수정할 게시글 정보 DTO (PostEditRequestDTO로 네이밍하는 것이 더 적절해 보입니다)
     * @return 수정된 게시글의 ID
     */
    @Override
    @Transactional // 쓰기 작업
    public Long update(PostEditResponseDTO responseDTO) {
        // 1. 영속성 컨텍스트에 게시글 엔티티 불러오기
        Post post = findById(responseDTO.getId()); // 내부적으로 findById 호출

        // 2. 엔티티 정보 변경 (더티 체킹 대상)
        updatePostDetails(post, responseDTO);

        // 트랜잭션 종료 시 변경 감지로 인해 UPDATE 쿼리 자동 실행
        return post.getId();
    }

    /**
     * ID와 Post 엔티티 객체를 받아 게시글을 수정합니다. (BaseService의 메서드 구현)
     * 현재는 사용되지 않음 (@Deprecated).
     *
     * @param id     수정할 게시글 ID
     * @param object 수정할 내용을 담은 Post 엔티티
     * @return 성공 시 0L (실제 구현 필요 시 수정)
     */
    @Override
    @Deprecated
    public Long update(Long id, Post object) {
        // 실제 구현이 필요하다면, id로 post를 찾고 object의 내용으로 업데이트하는 로직 추가
        return 0L;
    }


    // --- 게시글 삭제 (Delete) ---

    /**
     * ID로 특정 게시글을 삭제합니다. (BaseService의 메서드 구현)
     * 삭제 전 해당 게시글이 존재하는지 확인합니다.
     *
     * @param id 삭제할 게시글 ID
     * @return 삭제된 게시글의 ID
     * @throws RuntimeException 게시글을 찾을 수 없을 때
     */
    @Override
    @Transactional // 쓰기 작업
    public Long deleteById(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post with id " + id + " not found");
        }
        // 연관된 파일 등은 Cascade 또는 별도 로직으로 처리 필요
        postRepository.deleteById(id);
        return id;
    }


    // --- 게시글 좋아요 관련 기능 ---

    /**
     * 특정 게시글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     *
     * @param postId 게시글 ID
     * @param email  사용자 이메일
     * @return 작업 후 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    @Transactional // 쓰기 작업 (PostLike 엔티티 생성/삭제)
    @Override
    public boolean toggleLike(Long postId, String email) {
        Post post = findById(postId); // 게시글 조회
        User user = userService.findByEmail(email); // 사용자 조회

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 누른 경우: 좋아요 취소
            post.removeLike(existingLike.get()); // Post 엔티티의 연관관계 컬렉션에서 제거 (필요시)
            postLikeRepository.delete(existingLike.get());
            return false; // 좋아요 취소됨
        } else {
            // 좋아요를 누르지 않은 경우: 좋아요 추가
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            post.addLike(newLike); // Post 엔티티의 연관관계 컬렉션에 추가 (필요시)
            postLikeRepository.save(newLike);
            return true; // 좋아요 추가됨
        }
    }

    /**
     * 특정 게시글의 총 좋아요 수를 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 해당 게시글의 좋아요 수
     */
    @Override
    public long getLikeCount(Long postId) {
        Post post = findById(postId); // 게시글 존재 확인 및 조회
        return postLikeRepository.countByPost(post);
    }


    // --- 내부 헬퍼 메서드 ---

    /**
     * 게시글의 조회수를 1 증가시킵니다.
     * (public으로 선언되어 있지만, 주로 getPostDetailsAndIncreaseViews 내부에서 사용될 것으로 보임)
     *
     * @param postId 조회수를 증가시킬 게시글 ID
     */
    @Transactional // 쓰기 작업
    public void increaseViews(Long postId) {
        int updatedRows = postRepository.updateViewCount(postId);
        if (updatedRows == 0) {
            // ID에 해당하는 게시글이 없거나 업데이트에 실패한 경우 (필요시 예외 처리)
            // throw new RuntimeException("Failed to update view count for post id " + postId);
        }
    }

    /**
     * 기존 Post 엔티티의 내용을 PostEditResponseDTO의 정보로 업데이트합니다.
     * 파일 삭제 및 새 파일 업로드 로직을 포함합니다.
     * (private 메서드로 변경 가능성 있음)
     *
     * @param existingPost 업데이트할 기존 Post 엔티티 (영속 상태)
     * @param responseDTO  업데이트 정보를 담은 DTO
     */
    private void updatePostDetails(Post existingPost, PostEditResponseDTO responseDTO) {
        if (responseDTO.getTitle() != null) {
            existingPost.setTitle(responseDTO.getTitle());
        }
        if (responseDTO.getContent() != null) {
            existingPost.setContent(responseDTO.getContent());
        }
        if (responseDTO.getCategory() != null) { // DTO의 카테고리가 PostCategory 타입이라고 가정
            existingPost.setCategory(responseDTO.getCategory());
        }

        // 첨부된 파일 삭제 처리
        if (responseDTO.getDeletedAttachmentsId() != null && !responseDTO.getDeletedAttachmentsId().isEmpty()) {
            responseDTO.getDeletedAttachmentsId().forEach(fileId -> {
                // FileService를 통해 파일 실제 삭제 및 Post와의 연관관계 제거 로직 필요
                // 여기서는 Post 엔티티의 컬렉션에서만 제거하는 것으로 가정
                // fileService.deleteFile(fileId); // 실제 파일 삭제
                // existingPost.removeAttachmentById(fileId); // Post 엔티티 내 컬렉션 관리 메서드 호출
                existingPost.removeAttachment(fileService.getFile(fileId)); // File 엔티티를 가져와서 제거
            });
        }

        // 새로운 파일 업로드 처리
        if (responseDTO.getNewAttachments() != null && !responseDTO.getNewAttachments().isEmpty()) {
            uploadFiles(responseDTO.getNewAttachments(), existingPost);
        }
    }

    /**
     * MultipartFile 목록을 업로드하고 Post 엔티티와 연관관계를 설정합니다.
     * (private 메서드로 변경 가능성 있음)
     *
     * @param files 업로드할 MultipartFile 리스트
     * @param post  파일을 첨부할 Post 엔티티
     */
    private void uploadFiles(List<MultipartFile> files, Post post) {
        if (files != null && !files.isEmpty()) {
            // FileService를 통해 파일 저장 및 Post와의 연관관계 설정 (Post 엔티티에 파일 정보 추가)
            // 이 메서드는 Post 객체에 파일 정보를 추가(세팅)하는 역할을 한다고 가정
            fileService.addFileToPost(files, post);
        }
    }
}

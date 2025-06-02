package org.example.demo.service;

import org.example.demo.domain.Post;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // List import는 현재 사용되지 않지만, 향후 확장성을 위해 남겨둘 수 있습니다.

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 * BaseService<Post>를 상속하여 기본적인 CRUD 기능을 포함할 수 있습니다.
 */
public interface PostService extends BaseService<Post> {

    // --- 게시글 생성 ---

    /**
     * 새로운 게시글을 저장합니다.
     *
     * @param requestDTO 게시글 생성 요청 DTO
     * @param email      작성자 이메일
     * @param categoryId 카테고리 ID
     * @return 저장된 게시글의 ID
     */
    Long save(PostCreationRequestDTO requestDTO, String email, Long categoryId);


    // --- 게시글 조회 ---

    /**
     * ID로 특정 게시글을 조회하며, 작성자 정보를 함께 가져옵니다 (Fetch Join).
     *
     * @param id 조회할 게시글 ID
     * @return 게시글 엔티티 (작성자 정보 포함)
     */
    Post findPostWithUser(Long id);

    /**
     * ID로 특정 게시글을 조회하며, 작성자, 카테고리, 파일 정보를 함께 가져옵니다 (Fetch Join).
     *
     * @param id 조회할 게시글 ID
     * @return 게시글 엔티티 (작성자, 카테고리, 파일 정보 포함)
     */
    Post findPostWithUserAndCategoryAndFiles(Long id);

    /**
     * 특정 게시글의 상세 정보를 조회하고 조회수를 1 증가시킵니다.
     * (조회와 업데이트가 동시에 일어나는 서비스 메서드)
     *
     * @param postId 조회할 게시글 ID
     * @return 게시글 엔티티 (조회수 증가 후)
     */
    Post getPostDetailsAndIncreaseViews(Long postId);

    /**
     * 검색 조건과 페이징 정보를 기반으로 게시글 목록을 조회합니다.
     * 조회 전용 트랜잭션으로 실행됩니다.
     *
     * @param requestDTO 검색 조건 DTO
     * @param pageable   페이징 정보
     * @return 페이징된 게시글 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    Page<PostListResponseDTO> getPostList(PostSearchRequestDTO requestDTO, Pageable pageable);


    // --- 게시글 수정 ---

    /**
     * 기존 게시글의 정보를 수정합니다.
     *
     * @param responseDTO 수정할 게시글 정보 DTO (PostEditResponseDTO 네이밍은 PostUpdateRequestDTO가 더 적절해 보입니다)
     * @return 수정된 게시글의 ID
     */
    Long update(PostEditResponseDTO responseDTO); // DTO 네이밍 컨벤션 고려


    // --- 게시글 좋아요 관련 기능 ---

    /**
     * 특정 게시글에 대한 사용자의 좋아요 상태를 토글합니다. (좋아요/좋아요 취소)
     *
     * @param postId 게시글 ID
     * @param email  사용자 이메일
     * @return 좋아요 상태 변경 후 현재 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
     */
    boolean toggleLike(Long postId, String email);

    /**
     * 특정 게시글의 총 좋아요 수를 조회합니다.
     *
     * @param postId 게시글 ID
     * @return 해당 게시글의 좋아요 수
     */
    long getLikeCount(Long postId);

}

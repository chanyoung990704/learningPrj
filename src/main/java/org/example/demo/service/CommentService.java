package org.example.demo.service;

import org.example.demo.domain.Comment;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.response.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// import java.util.List; // 현재 인터페이스 시그니처에서는 사용되지 않음

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 * BaseService<Comment>를 상속하여 기본적인 CRUD 기능을 포함합니다.
 */
public interface CommentService extends BaseService<Comment> {

    // --- 댓글 생성 ---

    /**
     * 새로운 댓글(최상위 댓글)을 저장합니다.
     * 쓰기 작업이므로 트랜잭션 처리가 필요합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO
     * @param email      작성자 이메일
     * @param postId     댓글이 달릴 게시글 ID
     * @return 저장된 댓글의 ID
     */
    Long save(CommentCreationRequestDTO requestDTO, String email, Long postId);

    /**
     * 새로운 대댓글을 저장합니다.
     * 쓰기 작업이므로 트랜잭션 처리가 필요합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO (대댓글 내용)
     * @param email      작성자 이메일
     * @param postId     대댓글이 달릴 게시글 ID (부모 댓글의 게시글 ID와 일치해야 함)
     * @param parentId   부모 댓글 ID
     * @return 저장된 대댓글의 ID
     */
    Long saveReply(CommentCreationRequestDTO requestDTO, String email, Long postId, Long parentId);


    // --- 댓글 조회 ---

    /**
     * 댓글 ID로 특정 댓글을 조회하며, 작성자(User) 정보를 함께 가져옵니다 (Fetch Join 등 활용 가능).
     * (BaseService의 findById(Long id)와는 달리 사용자 정보를 포함하여 조회하는 특화된 메서드)
     *
     * @param commentId 조회할 댓글 ID
     * @return 조회된 Comment 엔티티 (작성자 정보 포함 가능성 있음)
     */
    Comment findCommentByIdWithUser(Long commentId);

    /**
     * 특정 게시글의 댓글 목록을 페이징하여 조회합니다 (답글 포함 계층 구조).
     *
     * @param postId   댓글을 조회할 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 DTO 목록 (CommentResponseDTO, 답글 포함 계층 구조)
     */
    Page<CommentResponseDTO> getCommentsWithReplies(Long postId, Pageable pageable);


    // --- 댓글 수정 ---

    /**
     * ID로 특정 댓글의 내용을 CommentCreationRequestDTO의 정보로 업데이트합니다.
     * 쓰기 작업이므로 트랜잭션 처리가 필요합니다.
     * (BaseService의 update(Long id, Comment object)와는 DTO를 받는 점에서 차이가 있음)
     *
     * @param id        업데이트할 댓글 ID
     * @param updateDto 업데이트할 댓글 내용을 가진 DTO (content 필드 등 사용)
     * @return 업데이트된 댓글의 ID
     */
    Long update(Long id, CommentCreationRequestDTO updateDto);

    // BaseService<Comment>로부터 상속받는 메서드들:
    // Long save(Comment comment);
    // Comment findById(Long id);
    // List<Comment> findAll(); // java.util.List 임포트가 필요할 수 있음 (만약 BaseService에 정의되어 있다면)
    // Long deleteById(Long id);
    // Long update(Long id, Comment comment); // 이 메서드는 위 DTO를 받는 update와 시그니처가 다름

    // (필요시) 댓글 좋아요 관련 메서드 시그니처 추가 가능
    // boolean toggleLike(Long commentId, Long userId);
}

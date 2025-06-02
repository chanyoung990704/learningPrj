package org.example.demo.repository;

import org.example.demo.domain.Comment;
import org.example.demo.dto.response.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 관련 데이터베이스 작업을 처리하는 리포지토리 인터페이스
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // --- 게시글 ID 기반 댓글 조회 (페이징 및 DTO 변환) ---

    /**
     * 게시글 ID로 최상위 댓글 목록을 페이징하여 조회합니다. (부모 댓글이 없는 댓글만)
     * Spring Data JPA 쿼리 메서드 기능을 사용합니다.
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 최상위 댓글 목록
     */
    Page<Comment> findCommentsByPostIdAndParentIsNull(Long postId, Pageable pageable);

    /**
     * 게시글 ID로 댓글 DTO 목록을 페이징하여 조회합니다.
     * 최신 댓글 순 (생성일시 내림차순, ID 내림차순)으로 정렬합니다.
     *
     * @param id       게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 DTO 목록 (CommentResponseDTO)
     */
    @Query("SELECT new org.example.demo.dto.response.CommentResponseDTO(" +
            "c.id, c.content, u.name, u.email, u.id, c.createdAt) " +
            "FROM Comment c " +
            "JOIN c.user u " + // 댓글 작성자 정보 JOIN
            "WHERE c.post.id = :id " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    Page<CommentResponseDTO> findCommentDTOsByPostId(@Param("id") Long id, Pageable pageable);


    // --- 게시글 ID 기반 댓글 목록 조회 (List, Fetch Join 활용) ---

    /**
     * 게시글 ID로 댓글 목록을 사용자 및 게시글 정보와 함께 조회합니다. (Fetch Join)
     *
     * @param id 게시글 ID
     * @return 댓글 엔티티 목록 (사용자, 게시글 정보 포함)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.post p " +
            "WHERE c.post.id = :id")
    List<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id);

    /**
     * 게시글 ID로 최상위 댓글 목록을 사용자, 게시글 정보와 함께 조회합니다. (Fetch Join)
     * 오래된 댓글 순 (생성일시 오름차순)으로 정렬합니다.
     * (메서드명에 Likes가 있지만, 쿼리에서는 직접적인 좋아요 수 로딩은 보이지 않음. 연관관계 매핑을 통해 접근 가능할 수 있음)
     *
     * @param id 게시글 ID
     * @return 최상위 댓글 엔티티 목록 (사용자, 게시글 정보 포함)
     */
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user u " +         // 댓글 작성자 정보 Fetch Join
            "LEFT JOIN FETCH c.post p " +         // 게시글 정보 Fetch Join
            "WHERE c.post.id = :id AND c.parent IS NULL " + // 최상위 댓글 조건
            "ORDER BY c.createdAt ASC")
    List<Comment> findCommentsByPostIdWithUserAndPostAndLikes(@Param("id") Long id);


    // --- 답글 조회 ---

    /**
     * 부모 댓글 ID 목록으로 답글 목록을 사용자 정보와 함께 조회합니다. (Fetch Join)
     * 부모 댓글 ID 기준 오름차순, 각 부모 내에서는 답글 생성일시 오름차순으로 정렬합니다.
     * (메서드명에 Likes가 있지만, 쿼리에서는 직접적인 좋아요 수 로딩은 보이지 않음.)
     *
     * @param parentIds 부모 댓글 ID 목록
     * @return 답글 엔티티 목록 (사용자 정보 포함)
     */
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user u " +         // 답글 작성자 정보 Fetch Join
            "WHERE c.parent.id IN :parentIds " +  // 부모 댓글 ID 목록 조건
            "ORDER BY c.parent.id, c.createdAt ASC")
    List<Comment> findRepliesByParentIdsWithLikes(@Param("parentIds") List<Long> parentIds);


    // --- 단일 댓글 조회 (댓글 ID 기반) ---

    /**
     * 댓글 ID로 특정 댓글을 사용자 정보와 함께 조회합니다. (Fetch Join)
     *
     * @param commentId 댓글 ID
     * @return Optional<Comment> 조회된 댓글 엔티티 (사용자 정보 포함, 존재하지 않을 수 있음)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.id = :id")
    Optional<Comment> findCommentByIdWithUser(@Param("id") Long commentId);
}

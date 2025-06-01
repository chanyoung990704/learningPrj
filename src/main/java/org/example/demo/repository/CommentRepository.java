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

    /**
     * 게시글 ID로 댓글 목록을 사용자 및 게시글 정보와 함께 조회합니다.
     *
     * @param id 게시글 ID
     * @return 댓글 목록
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.post p " +
            "WHERE c.post.id = :id")
    List<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id);

    /**
     * ID로 댓글을 사용자 정보와 함께 조회합니다.
     *
     * @param commentId 댓글 ID
     * @return 조회된 댓글 (존재하지 않을 수 있음)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.id = :id")
    Optional<Comment> findCommentByIdWithUser(@Param("id") Long commentId);

//    /**
//     * 게시글 ID로 댓글 목록을 페이징하여 사용자 및 게시글 정보와 함께 조회합니다.
//     *
//     * @param id       게시글 ID
//     * @param pageable 페이징 정보
//     * @return 페이징된 댓글 목록
//     */
//    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.post p " +
//            "WHERE c.post.id = :id ORDER BY c.createdAt DESC, c.id DESC")
//    Page<Comment> findCommentsByPostIdWithUserAndPost(@Param("id") Long id, Pageable pageable);

    /**
     * 게시글 ID로 댓글 DTO 목록을 페이징하여 조회합니다.
     *
     * @param id       게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 DTO 목록
     */
    @Query("SELECT new org.example.demo.dto.response.CommentResponseDTO(" +
            "c.id, c.content, u.name, u.email, u.id, c.createdAt) " +
            "FROM Comment c " +
            "JOIN c.user u " +
            "WHERE c.post.id = :id " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    Page<CommentResponseDTO> findCommentDTOsByPostId(@Param("id") Long id, Pageable pageable);

//    /**
//     * 부모 댓글 ID로 대댓글 DTO 목록을 조회합니다.
//     *
//     * @param parentId 부모 댓글 ID
//     * @return 대댓글 DTO 목록
//     */
//    @Query("SELECT NEW org.example.demo.dto.response.CommentResponseDTO(" +
//            "c.id, c.content, u.name, u.email, u.id, c.createdAt, " +
//            "c.parent.id, null) " +  // parentId, replies는 null
//            "FROM Comment c " +
//            "JOIN c.user u " +
//            "WHERE c.parent.id = :parentId " +
//            "ORDER BY c.createdAt ASC")
//    List<CommentResponseDTO> findRepliesByParentId(@Param("parentId") Long parentId);
//
//    /**
//     * 게시글의 모든 댓글 DTO 목록을 조회합니다.
//     *
//     * @param postId 게시글 ID
//     * @return 댓글 DTO 목록
//     */
//    @Query("SELECT NEW org.example.demo.dto.response.CommentResponseDTO(" +
//            "c.id, c.content, u.name, u.email, u.id, c.createdAt, " +
//            "c.parent.id, null) " +  // parentId, replies는 null
//            "FROM Comment c " +
//            "JOIN c.user u " +
//            "WHERE c.post.id = :postId " +
//            "ORDER BY " +
//            "  CASE WHEN c.parent IS NULL THEN 0 ELSE 1 END, " +  // 부모 댓글 먼저
//            "  c.createdAt DESC")  // 부모는 최신순
//    List<CommentResponseDTO> findAllComments(@Param("postId") Long postId);
//
//    /**
//     * 부모 댓글 DTO 목록을 페이징하여 조회합니다.
//     *
//     * @param postId   게시글 ID
//     * @param pageable 페이징 정보
//     * @return 페이징된 부모 댓글 DTO 목록
//     */
//    @Query("SELECT NEW org.example.demo.dto.response.CommentResponseDTO(" +
//            "c.id, c.content, u.name, u.email, u.id, c.createdAt, " +
//            "c.parent.id, null) " +  // parentId, replies는 null
//            "FROM Comment c " +
//            "JOIN c.user u " +
//            "WHERE c.post.id = :postId AND c.parent IS NULL " +  // 부모 댓글만 조회
//            "ORDER BY c.createdAt DESC")
//    Page<CommentResponseDTO> findParentComments(@Param("postId") Long postId, Pageable pageable);
//
//    /**
//     * 부모 댓글 ID 목록에 해당하는 대댓글 DTO 목록을 조회합니다.
//     *
//     * @param parentIds 부모 댓글 ID 목록
//     * @return 대댓글 DTO 목록
//     */
//    @Query("SELECT NEW org.example.demo.dto.response.CommentResponseDTO(" +
//            "c.id, c.content, u.name, u.email, u.id, c.createdAt, " +
//            "c.parent.id, null) " +  // parentId, replies는 null
//            "FROM Comment c " +
//            "JOIN c.user u " +
//            "WHERE c.parent.id IN :parentIds " +  // 부모 댓글 ID 목록으로 조회
//            "ORDER BY c.parent.id, c.createdAt ASC")  // 부모 댓글별로 묶어서 오래된 순으로 정렬
//    List<CommentResponseDTO> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);


    Page<Comment> findCommentsByPostIdAndParentIsNull(Long postId, Pageable pageable);
}
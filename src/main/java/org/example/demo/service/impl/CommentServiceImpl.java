package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.Comment;
import org.example.demo.domain.CommentLike;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.response.CommentResponseDTO;
import org.example.demo.repository.CommentLikeRepository;
import org.example.demo.repository.CommentRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
@Transactional(readOnly = true) // 클래스 레벨 기본 트랜잭션: 읽기 전용
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final CommentLikeRepository commentLikeRepository;

    // --- 댓글 생성 (Create) ---

    /**
     * 새로운 댓글(최상위 댓글)을 저장합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO
     * @param email     작성자 이메일
     * @param postId    댓글이 달릴 게시글 ID
     * @return 저장된 댓글의 ID
     */
    @Override
    @Transactional // 쓰기 작업이므로 트랜잭션 오버라이드
    public Long save(CommentCreationRequestDTO requestDTO, String email, Long postId) {
        Post post = postService.findById(postId); // 댓글이 달릴 게시글 조회
        User user = userService.findByEmail(email); // 댓글 작성자 조회

        Comment comment = Comment.builder()
                .content(requestDTO.getContent())
                .user(user)
                // post는 Comment 엔티티 내에서 addComment 시 자동 설정되거나, 명시적으로 설정
                .build();

        post.addComment(comment); // 게시글에 댓글 추가 (연관관계 설정)
        return save(comment);    // 내부 save(Comment) 메서드 호출하여 저장
    }

    /**
     * 새로운 대댓글을 저장합니다.
     *
     * @param requestDTO 댓글 생성 요청 DTO (대댓글 내용)
     * @param email     작성자 이메일
     * @param postId    대댓글이 달릴 게시글 ID (부모 댓글의 게시글 ID와 일치해야 함)
     * @param parentId  부모 댓글 ID
     * @return 저장된 대댓글의 ID
     * @throws RuntimeException 부모 댓글이 존재하지 않거나, 요청된 postId와 부모 댓글의 postId가 일치하지 않는 경우
     */
    @Override
    @Transactional // 쓰기 작업
    public Long saveReply(CommentCreationRequestDTO requestDTO, String email, Long postId, Long parentId) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentId));

        // 부모 댓글이 속한 게시글과 현재 요청의 게시글 ID가 일치하는지 검증
        if (!parentComment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Post ID (" + postId + ") does not match the parent comment's post ID (" + parentComment.getPost().getId() + ")");
        }

        User user = userService.findByEmail(email); // 대댓글 작성자 조회
        Comment reply = Comment.builder()
                .content(requestDTO.getContent())
                .user(user)
                .parent(parentComment) // 부모 댓글 설정
                // post는 부모 댓글을 통해 접근 가능하거나, 명시적으로 설정
                .build();

        parentComment.getPost().addComment(reply); // 게시글에 대댓글 추가 (연관관계 설정)
        // 또는 parentComment.addChildComment(reply); 와 같이 부모 댓글에 자식 댓글 추가
        return save(reply); // 내부 save(Comment) 메서드 호출하여 저장
    }

    /**
     * Comment 엔티티 객체를 직접 저장합니다. (주로 내부에서 사용)
     *
     * @param comment 저장할 Comment 엔티티
     * @return 저장된 댓글의 ID
     */
    @Override
    @Transactional // 쓰기 작업
    public Long save(Comment comment) {
        return commentRepository.save(comment).getId();
    }


    // --- 댓글 조회 (Read) ---

    /**
     * ID로 특정 댓글을 조회합니다.
     *
     * @param id 조회할 댓글 ID
     * @return 조회된 Comment 엔티티
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Comment with id " + id + " not found"));
    }

    /**
     * 모든 댓글 목록을 조회합니다. (현재 구현은 비어있는 리스트 반환 - 필요시 실제 구현)
     *
     * @return 모든 Comment 엔티티 목록 (현재는 빈 리스트)
     */
    @Override
    public List<Comment> findAll() {
        // 실제 모든 댓글을 조회하는 로직이 필요하다면 commentRepository.findAll() 등으로 구현
        return List.of(); // 현재는 비어있는 리스트 반환
    }

    /**
     * 댓글 ID로 특정 댓글을 조회하며, 작성자(User) 정보를 함께 Fetch Join 합니다.
     *
     * @param commentId 조회할 댓글 ID
     * @return 조회된 Comment 엔티티 (작성자 정보 포함)
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    public Comment findCommentByIdWithUser(Long commentId) {
        return commentRepository.findCommentByIdWithUser(commentId).orElseThrow(() ->
                new RuntimeException("Comment with id " + commentId + " not found"));
    }

    /**
     * 특정 게시글의 댓글 목록을 페이징하여 조회합니다 (답글 포함 계층 구조).
     * 최상위 댓글을 기준으로 페이징하고, 각 최상위 댓글에 대한 답글들을 재귀적으로 DTO에 포함시킵니다.
     *
     * @param postId   댓글을 조회할 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 DTO 목록 (CommentResponseDTO, 답글 포함 계층 구조)
     */
    @Override
    public Page<CommentResponseDTO> getCommentsWithReplies(Long postId, Pageable pageable) {
        // 1. 특정 게시글의 최상위 댓글(부모가 없는 댓글)을 페이징하여 조회합니다.
        Page<Comment> rootCommentsPage = commentRepository.findCommentsByPostIdAndParentIsNull(postId, pageable);

        // 2. 조회된 최상위 댓글 엔티티 목록을 CommentResponseDTO로 변환합니다.
        //    CommentResponseDTO.toDtoWithChildren 정적 메서드가 재귀적으로 자식 댓글(답글)까지 DTO로 변환한다고 가정합니다.
        Page<CommentResponseDTO> commentResponseDTOPage = rootCommentsPage.map(CommentResponseDTO::toDtoWithChildren);

        // 3. 좋아요 수 등의 추가 정보가 필요하다면 여기서 각 DTO에 설정할 수 있습니다.
        //    (예: commentResponseDTOPage.getContent().forEach(dto -> dto.setLikeCount(...)))

        return commentResponseDTOPage;
    }


    // --- 댓글 수정 (Update) ---

    /**
     * ID로 특정 댓글의 내용을 Comment 엔티티 객체의 정보로 업데이트합니다.
     * (주로 ID와 새로운 내용만으로 업데이트하는 경우가 많으므로, DTO를 받는 메서드가 더 일반적일 수 있습니다.)
     *
     * @param id      업데이트할 댓글 ID
     * @param comment 업데이트할 내용을 담은 Comment 엔티티 (content 필드만 사용)
     * @return 업데이트된 댓글의 ID
     */
    @Override
    @Transactional // 쓰기 작업
    public Long update(Long id, Comment comment) {
        Comment existingComment = findById(id); // 영속성 컨텍스트에 있는 댓글 엔티티 조회
        if (comment.getContent() != null) {
            existingComment.setContent(comment.getContent()); // 내용 변경 (더티 체킹 대상)
        }
        // 트랜잭션 종료 시 변경 감지로 UPDATE 쿼리 자동 실행
        return existingComment.getId();
    }

    /**
     * ID로 특정 댓글의 내용을 CommentCreationRequestDTO의 정보로 업데이트합니다.
     *
     * @param id       업데이트할 댓글 ID
     * @param updateDto 업데이트할 댓글 내용을 가진 DTO (content 필드 사용)
     * @return 업데이트된 댓글의 ID
     */
    @Override
    @Transactional // 쓰기 작업
    public Long update(Long id, CommentCreationRequestDTO updateDto) {
        Comment existingComment = findById(id); // 영속성 컨텍스트에 있는 댓글 엔티티 조회
        if (updateDto != null && updateDto.getContent() != null) {
            existingComment.setContent(updateDto.getContent()); // 내용 변경 (더티 체킹 대상)
        }
        // 트랜잭션 종료 시 변경 감지로 UPDATE 쿼리 자동 실행
        return existingComment.getId();
    }


    // --- 댓글 삭제 (Delete) ---

    /**
     * ID로 특정 댓글을 삭제합니다.
     * 해당 댓글에 답글이 있는 경우, 답글 처리 정책(예: 함께 삭제, 연결 끊기 등)을 고려해야 합니다.
     * (현재 구현은 해당 댓글만 삭제)
     *
     * @param id 삭제할 댓글 ID
     * @return 삭제된 댓글의 ID
     * @throws RuntimeException 해당 ID의 댓글이 존재하지 않는 경우
     */
    @Override
    @Transactional // 쓰기 작업
    public Long deleteById(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment with id " + id + " not found");
        }
        // TODO: 답글 처리 정책 결정 및 구현 (예: Cascade 설정, 또는 수동으로 답글 삭제/연결 해제)
        // TODO: 좋아요 등 연관된 데이터 처리 정책 결정 및 구현
        commentRepository.deleteById(id);
        return id;
    }


    // --- 댓글 좋아요 관련 기능 ---

    /**
     * 특정 댓글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     * (public으로 선언되어 있지만, CommentService 인터페이스에 정의되어 있지 않음. 필요시 인터페이스에 추가)
     *
     * @param commentId 좋아요를 토글할 댓글 ID
     * @param userId    좋아요를 누르는 사용자 ID
     * @return 작업 후 좋아요 상태 (true: 좋아요 추가됨, false: 좋아요 취소됨)
     */
    @Transactional // 쓰기 작업 (CommentLike 엔티티 생성/삭제)
    public boolean toggleLike(Long commentId, Long userId) {
        Comment comment = findById(commentId); // 댓글 조회
        User user = userService.findById(userId);   // 사용자 조회

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 누른 경우: 좋아요 취소
            comment.removeLike(existingLike.get()); // Comment 엔티티의 연관관계 컬렉션에서 제거 (양방향 매핑 시 필요)
            commentLikeRepository.delete(existingLike.get());
            return false; // 좋아요 취소됨
        } else {
            // 좋아요를 누르지 않은 경우: 좋아요 추가
            CommentLike newLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            comment.addLike(newLike); // Comment 엔티티의 연관관계 컬렉션에 추가 (양방향 매핑 시 필요)
            commentLikeRepository.save(newLike);
            return true; // 좋아요 추가됨
        }
    }
}

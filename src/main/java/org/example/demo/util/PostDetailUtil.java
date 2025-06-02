package org.example.demo.util;

import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.dto.response.CommentResponseDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 상세 페이지 관련 유틸리티 클래스
 * 게시글 상세 정보와 댓글을 조회하여 모델에 추가하는 기능을 제공합니다.
 */
public class PostDetailUtil {

    // 기본 댓글 페이지 크기
    private static final int DEFAULT_COMMENT_PAGE_SIZE = 10;

    /**
     * 게시글 상세 정보와 페이징된 댓글을 모델에 추가합니다.
     *
     * @param postId 게시글 ID
     * @param postService 게시글 서비스
     * @param commentService 댓글 서비스
     * @param model 뷰에 전달할 모델 객체
     * @param commentPageNumber 댓글 페이지 번호 (0부터 시작)
     * @return 게시글 상세 응답 DTO
     */
    public static PostDetailResponseDTO putPostDetailToModel(Long postId, PostService postService,
                                                             CommentService commentService, Model model,
                                                             int commentPageNumber) {
        // 게시글 조회 (사용자, 카테고리, 파일 정보 포함)
        Post post = postService.getPostDetailsAndIncreaseViews(postId);

        // 댓글 페이징 정보 설정 (생성일자 내림차순, ID 내림차순)
        Pageable pageable = PageRequest.of(commentPageNumber, DEFAULT_COMMENT_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 계층형 댓글 페이징 조회
        Page<CommentResponseDTO> commentPage = commentService.getCommentsWithReplies(postId, pageable);

        // 파일 분류 (이미지/기타)
        Map<Boolean, List<File>> fileClassification = post.getFiles().stream()
                .collect(Collectors.partitioningBy(file -> file.getFileType().startsWith("image")));

        // PostDetailResponseDTO 생성
// PostDetailResponseDTO 생성
        PostDetailResponseDTO postResponseDTO = buildPostDetailResponse(post, commentPage, fileClassification, postService);
        // 모델에 추가
        model.addAttribute("post", postResponseDTO);

        return postResponseDTO;
    }

    /**
     * 기본 페이지(0)의 게시글 상세 정보와 페이징된 댓글을 모델에 추가합니다.
     *
     * @param postId 게시글 ID
     * @param postService 게시글 서비스
     * @param commentService 댓글 서비스
     * @param model 뷰에 전달할 모델 객체
     * @return 게시글 상세 응답 DTO
     */
    public static PostDetailResponseDTO putPostDetailToModel(Long postId, PostService postService, 
                                                    CommentService commentService, Model model) {
        return putPostDetailToModel(postId, postService, commentService, model, 0);
    }

    /**
     * PostDetailResponseDTO를 생성합니다.
     *
     * @param post 게시글 엔티티
     * @param commentPage 페이징된 댓글
     * @param fileClassification 분류된 파일 맵 (이미지/기타)
     * @return PostDetailResponseDTO 객체
     */
    private static PostDetailResponseDTO buildPostDetailResponse(Post post,
                                                                 Page<CommentResponseDTO> commentPage,
                                                                 Map<Boolean, List<File>> fileClassification,
                                                                 PostService postService) {
        return PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .time(post.getUpdatedAt())
                .category(post.getCategory())
                .author(post.getUser().getName())
                .email(post.getUser().getEmail())
                .comments(commentPage)
                .imageAttachments(fileClassification.get(true))
                .otherAttachments(fileClassification.get(false))
                .likeCount(postService.getLikeCount(post.getId()))
                .build();
    }
}

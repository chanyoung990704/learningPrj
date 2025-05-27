package org.example.demo.util;

import org.example.demo.controller.PostController;
import org.example.demo.domain.Comment;
import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.dto.response.CommentToPostResponseDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 상세 페이지 관련 유틸리티 클래스
 */
public class PostDetailUtil {

    /**
     * 게시글 상세 정보와 페이징된 댓글을 모델에 추가
     * @param postId 게시글 ID
     * @param postService 게시글 서비스
     * @param commentService 댓글 서비스
     * @param model 모델
     * @param commentPageNumber 댓글 페이지 번호
     * @return 게시글 상세 응답 DTO
     */
    public static PostDetailResponseDTO putPostDetailToModel(Long postId, PostService postService, 
                                                     CommentService commentService, Model model,
                                                     int commentPageNumber) {
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId);
        // 페이지 객체 생성 (최신순으로 10개)
        Pageable pageable = PageRequest.of(commentPageNumber, 10, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        // 페이징된 댓글 목록 조회
        Page<Comment> commentPage = commentService.findCommentsByPostIdWithUserAndPost(post.getId(), pageable);
        
        // Comment 엔티티를 CommentToPostResponseDTO로 변환
        List<CommentToPostResponseDTO> commentDtos = commentPage.getContent().stream()
            .map(comment -> CommentToPostResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getUser().getName())
                .email(comment.getUser().getEmail())
                .time(comment.getCreatedAt())
                .build())
            .collect(Collectors.toList());
            
        // 변환된 CommentToPostResponseDTO 목록을 포함한 Page 객체 생성
        Page<CommentToPostResponseDTO> commentDtoPage = new PageImpl<>(
            commentDtos, 
            commentPage.getPageable(), 
            commentPage.getTotalElements()
        );
        
        // 변환된 DTO를 모델에 추가
        model.addAttribute("commentPage", commentDtoPage);
        
        // 파일 분류 (이미지/기타)
        Map<Boolean, List<File>> imageFile = post.getFiles().stream()
            .collect(Collectors.partitioningBy(file -> file.getFileType().startsWith("image")));
        
        // PostDetailResponseDTO 직접 생성
        PostDetailResponseDTO postResponseDTO = PostDetailResponseDTO.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .time(post.getUpdatedAt())
            .category(post.getCategory())
            .author(post.getUser().getName())
            .email(post.getUser().getEmail())
            .comments(commentDtos)
            .imageAttachments(imageFile.get(true))
            .otherAttachments(imageFile.get(false))
            .build();
            
        model.addAttribute("post", postResponseDTO);
        
        return postResponseDTO;
    }
    
    /**
     * 기본 페이지(0)의 게시글 상세 정보와 페이징된 댓글을 모델에 추가
     * @param postId 게시글 ID
     * @param postService 게시글 서비스
     * @param commentService 댓글 서비스
     * @param model 모델
     * @return 게시글 상세 응답 DTO
     */
    public static PostDetailResponseDTO putPostDetailToModel(Long postId, PostService postService, 
                                                    CommentService commentService, Model model) {
        return putPostDetailToModel(postId, postService, commentService, model, 0);
    }
}

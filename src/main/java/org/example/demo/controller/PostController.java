package org.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.aop.TimeTrace;
import org.example.demo.aop.VerifyPostOwner;
import org.example.demo.domain.File;
import org.example.demo.domain.Post;
import org.example.demo.dto.request.CommentCreationRequestDTO;
import org.example.demo.dto.request.PostCreationRequestDTO;
import org.example.demo.dto.request.PostSearchRequestDTO;
import org.example.demo.dto.response.PostDetailResponseDTO;
import org.example.demo.dto.response.PostEditResponseDTO;
import org.example.demo.dto.response.PostListResponseDTO;
import org.example.demo.repository.PostRepository;
import org.example.demo.service.CommentService;
import org.example.demo.service.PostService;
import org.example.demo.service.impl.FileService;
import org.example.demo.service.impl.FileStore;
import org.example.demo.util.PostDetailUtil;
import org.example.demo.util.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts") // 이 컨트롤러의 모든 요청은 "/posts"를 기본 경로로 가짐
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final FileService fileService; // FileService 인터페이스 사용 권장
    private final FileStore fileStore;     // FileStore 인터페이스 사용 권장

    // === 게시글 목록 조회 (Read - List) ===

    /**
     * (GET) 게시글 목록 페이지를 조회합니다 (검색 및 페이징 기능 포함).
     */
    @GetMapping
    @TimeTrace // AOP: 메서드 실행 시간 측정
    public String showPostList(@ModelAttribute(value = "searchRequestDTO") PostSearchRequestDTO searchRequestDTO, // 검색 조건 DTO
                               @PageableDefault(sort = {"createdAt"}, size = PostRepository.DEFAULT_PAGE_SIZE, direction = Sort.Direction.DESC) Pageable pageable, // 페이징 및 정렬 정보
                               Model model) {
        Page<PostListResponseDTO> postPage = postService.getPostList(searchRequestDTO, pageable); // 게시글 목록 서비스 호출
        model.addAttribute("posts", postPage); // 모델에 게시글 페이지 추가
        return "post/post-list"; // 게시글 목록 뷰 반환
    }

    // === 게시글 상세 조회 (Read - Detail) ===

    /**
     * (GET) 특정 게시글의 상세 페이지를 조회합니다 (댓글 페이징 포함).
     */
    @GetMapping("/{postId}")
    public String showPostDetail(@PathVariable("postId") Long postId, // 경로 변수: 게시글 ID
                                 @RequestParam(value = "commentPage", defaultValue = "0") int commentPage, // 요청 파라미터: 댓글 페이지 번호
                                 Model model) {
        PostDetailResponseDTO postResponseDTO = PostDetailUtil.putPostDetailToModel(postId, postService, commentService, model, commentPage); // 게시글 및 댓글 정보 모델에 추가
        registerPostDetailModelAttributes(model, postResponseDTO); // 댓글 폼 등 추가 모델 속성 등록
        return "post/post-detail"; // 게시글 상세 뷰 반환
    }

    // === 게시글 생성 (Create) ===

    /**
     * (GET) 새 게시글 작성 폼 페이지를 반환합니다.
     */
    @GetMapping("/new")
    public String saveForm(@ModelAttribute("postRequestDTO") PostCreationRequestDTO postCreationRequestDTO) { // 폼 바인딩용 빈 DTO
        return "post/post-form"; // 게시글 작성 폼 뷰 반환
    }

    /**
     * (POST) 새 게시글을 저장합니다.
     */
    @PostMapping // "/posts" 경로에 대한 POST 요청 처리
    public String save(@AuthenticationPrincipal Object principal, // 현재 인증 사용자
                       @Validated @ModelAttribute("postRequestDTO") PostCreationRequestDTO postCreationRequestDTO, // 폼 데이터 바인딩 및 유효성 검증
                       BindingResult bindingResult, // 유효성 검증 결과
                       HttpServletResponse response) throws IOException { // HTTP 응답 객체 (현재는 미사용, 향후 쿠키 설정 등에 사용 가능)
        if (bindingResult.hasErrors()) { // 유효성 검사 실패 시
            return "post/post-form"; // 작성 폼으로 다시 이동
        }
        String email = SecurityUtils.extractEmailFromPrincipal(principal); // 사용자 이메일 추출
        Long postId = postService.save(postCreationRequestDTO, email, postCreationRequestDTO.getCategoryId()); // 게시글 저장 서비스 호출
        return "redirect:/posts/" + postId; // 저장된 게시글 상세 페이지로 리다이렉트
    }

    // === 게시글 수정 (Update) ===

    /**
     * (GET) 기존 게시글 수정 폼 페이지를 반환합니다.
     */
    @GetMapping("/{postId}/edit")
    @VerifyPostOwner // AOP: 게시글 소유자 검증
    public String editForm(@PathVariable("postId") Long postId, Model model) {
        Post post = postService.findPostWithUserAndCategoryAndFiles(postId); // 수정할 게시글 정보 조회 (파일 포함)
        PostEditResponseDTO postEditResponseDTO = toPostEditResponseDTO(post); // 조회된 엔티티를 수정 폼 DTO로 변환
        model.addAttribute("postEditResponseDTO", postEditResponseDTO); // 모델에 DTO 추가
        return "post/post-edit"; // 게시글 수정 폼 뷰 반환
    }

    /**
     * (PUT) 기존 게시글 정보를 수정합니다.
     */
    @PutMapping("/{postId}") // "/posts/{postId}" 경로에 대한 PUT 요청 처리
    @VerifyPostOwner // AOP: 게시글 소유자 검증
    public String editPost(@PathVariable("postId") Long postId, // 경로 변수: 게시글 ID (메서드 내에서 직접 사용되진 않음, 경로 매칭용)
                           @Validated @ModelAttribute("postEditResponseDTO") PostEditResponseDTO responseDTO, // 폼 데이터 바인딩 및 유효성 검증
                           BindingResult bindingResult){
        if(bindingResult.hasErrors()){ // 유효성 검사 실패 시
            responseDTO.getNewAttachments().clear(); // 새 첨부파일 목록 초기화 (선택적, 오류 시 파일 유지하지 않도록)
            return "/post/post-edit"; // 수정 폼으로 다시 이동
        }
        postService.update(responseDTO); // 게시글 수정 서비스 호출
        return "redirect:/posts/" + responseDTO.getId(); // 수정된 게시글 상세 페이지로 리다이렉트 (responseDTO에서 ID 사용)
    }

    // === 게시글 삭제 (Delete) ===

    /**
     * (DELETE) 특정 게시글을 삭제합니다.
     */
    @DeleteMapping("/{postId}") // "/posts/{postId}" 경로에 대한 DELETE 요청 처리
    @VerifyPostOwner // AOP: 게시글 소유자 검증
    public String deletePost(@PathVariable("postId") Long postId) {
        postService.deleteById(postId); // 게시글 삭제 서비스 호출
        return "redirect:/posts"; // 게시글 목록 페이지로 리다이렉트
    }

    // === 게시글 좋아요 기능 (Like) ===

    /**
     * (POST) API: 특정 게시글의 좋아요 상태를 토글합니다.
     */
    @PostMapping("/{id}/like") // "/posts/{id}/like" 경로에 대한 POST 요청 처리
    @ResponseBody // 응답 본문을 JSON 등으로 직접 반환
    public Map<String, Object> toggleLike(
            @PathVariable Long id, // 경로 변수: 게시글 ID
            @AuthenticationPrincipal Object principal) { // 현재 인증 사용자

        String email = SecurityUtils.extractEmailFromPrincipal(principal); // 사용자 이메일 추출
        boolean isLiked = postService.toggleLike(id, email); // 좋아요 토글 서비스 호출
        Long likeCount = postService.getLikeCount(id); // 현재 좋아요 수 조회 서비스 호출

        Map<String, Object> response = new HashMap<>(); // API 응답용 Map 객체
        response.put("success", true);
        response.put("isLiked", isLiked); // 현재 좋아요 상태
        response.put("likeCount", likeCount); // 현재 좋아요 수

        return response; // JSON 형태로 응답
    }

    // === 파일/이미지 다운로드 ===

    /**
     * (GET) 저장된 이미지를 다운로드(표시)합니다.
     */
    @ResponseBody // 응답 본문을 리소스 데이터로 직접 반환
    @GetMapping("/uploads/images/{filename}") // "/posts/uploads/images/{filename}" 경로
    public Resource downloadImage(@PathVariable("filename") String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename)); // 파일 저장소 경로에서 리소스 생성
    }

    /**
     * (GET) 저장된 파일을 다운로드합니다.
     */
    @ResponseBody // 응답 본문을 리소스 데이터로 직접 반환
    @GetMapping("/uploads/files/{id}") // "/posts/uploads/files/{id}" 경로
    public Resource downloadFile(@PathVariable("id") Long fileId, // 경로 변수: 파일 ID (변수명 fileId로 변경 권장)
                                 HttpServletResponse response) throws MalformedURLException {
        File file = fileService.getFile(fileId); // 파일 ID로 파일 정보 조회
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(file.getStoredName())); // 파일 저장소 경로에서 리소스 생성
        String contentDisposition = buildContentDispositionHeader(file.getOriginalName()); // Content-Disposition 헤더 생성
        response.setHeader("Content-Disposition", contentDisposition); // 응답 헤더에 설정 (다운로드 시 파일명 지정)
        return resource;
    }


    // === 내부 유틸리티 메서드 ===

    /**
     * (private) 게시글 상세 페이지에 필요한 추가 모델 속성(댓글 폼 등)을 등록합니다.
     */
    private void registerPostDetailModelAttributes(Model model, PostDetailResponseDTO postResponseDTO) {
        model.addAttribute("post", postResponseDTO); // 게시글 상세 정보
        // 댓글 및 답글 작성/수정을 위한 빈 DTO 객체들
        if (!model.containsAttribute("commentAddRequest")) {
            model.addAttribute("commentAddRequest", CommentCreationRequestDTO.builder().build());
        }
        if (!model.containsAttribute("replyAddRequest")) {
            model.addAttribute("replyAddRequest", CommentCreationRequestDTO.builder().build());
        }
        if (!model.containsAttribute("commentEditRequest")) {
            model.addAttribute("commentEditRequest", CommentCreationRequestDTO.builder().build());
        }
    }

    /**
     * (private) Post 엔티티를 PostEditResponseDTO로 변환합니다.
     */
    private PostEditResponseDTO toPostEditResponseDTO(Post post) {
        return PostEditResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .attachments(post.getFiles()) // 기존 첨부파일 목록
                .build();
    }

    /**
     * (private) 파일 다운로드를 위한 Content-Disposition 헤더 문자열을 생성합니다.
     */
    private String buildContentDispositionHeader(String originalFileName) {
        String encodedOriginFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8); // 파일명 URL 인코딩
        return "attachment; filename=\"" + encodedOriginFileName + "\""; // "첨부파일로 다운로드; 파일명=" 형식
    }
}
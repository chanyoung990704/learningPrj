package org.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.dto.request.UserRegisterRequestDTO;
import org.example.demo.oauth2.CustomOAuth2User;
import org.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${app.admin.secret.key}")
    private String adminSecretKey;

    @Value("${app.admin.secret.value}")
    private String adminSecretValue;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String redirectUrl, HttpSession session) {
        // 리다이렉트 URL이 있으면 세션에 저장 (OAuth2 로그인 성공 후 사용)
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            session.setAttribute("REDIRECT_URI", redirectUrl);
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String registerForm(@ModelAttribute("registerDTO") UserRegisterRequestDTO requestDTO) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Validated @ModelAttribute("registerDTO") UserRegisterRequestDTO registerDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // 오류 있는 경우
        if(result.hasErrors()){
            return "register";
        }

        // 관리자 회원가입 요청 확인
        String adminSecretHeader = request.getHeader(adminSecretKey);
        boolean isAdmin = adminSecretHeader != null ? adminSecretHeader.equals(adminSecretValue) : false;

        userService.save(UserRegisterRequestDTO.toUser(registerDto, isAdmin));
        // 다음 번 뷰에서만 필요한 모델 속성이기에 Flash
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다. 로그인 해주세요.");
        return "redirect:/login";
    }
    
    @GetMapping("/oauth2-info")
    public String getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) oAuth2User;
            model.addAttribute("name", customOAuth2User.getName());
            model.addAttribute("email", customOAuth2User.getEmail());
            model.addAttribute("provider", customOAuth2User.getAttributes().get("provider"));
            
            log.info("OAuth2 사용자 정보: {}", customOAuth2User);
        } else if (oAuth2User != null) {
            model.addAttribute("attributes", oAuth2User.getAttributes());
            log.info("기본 OAuth2 사용자 정보: {}", oAuth2User.getAttributes());
        }
        
        return "oauth2-info";
    }
}

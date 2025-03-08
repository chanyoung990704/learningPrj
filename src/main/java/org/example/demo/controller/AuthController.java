package org.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.dto.request.UserRegisterRequestDTO;
import org.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String login() {
        return "login";
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
}

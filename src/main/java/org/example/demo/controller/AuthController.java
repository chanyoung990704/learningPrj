package org.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.UserRegisterRequestDTO;
import org.example.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

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
                           RedirectAttributes redirectAttributes) {
        // 오류 있는 경우
        if(result.hasErrors()){
            return "/register";
        }

        userService.save(UserRegisterRequestDTO.toUser(registerDto));
        // 다음 번 뷰에서만 필요한 모델 속성이기에 Flash
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다. 로그인 해주세요.");
        return "redirect:/login";
    }
}

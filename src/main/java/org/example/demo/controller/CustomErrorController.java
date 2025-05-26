package org.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 에러 관련 정보 로깅
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        log.error("에러 발생: 상태 코드={}, 메시지={}, URI={}", status, message, uri);
        if (exception != null) {
            log.error("예외 정보: ", (Throwable) exception);
        }
        
        // 모델에 에러 정보 추가
        model.addAttribute("status", status != null ? status : "999");
        model.addAttribute("message", message != null ? message : "알 수 없는 오류가 발생했습니다");
        model.addAttribute("timestamp", java.time.LocalDateTime.now());
        model.addAttribute("uri", uri);
        
        // 콘솔에 디버그 정보 출력
        System.out.println("에러 컨트롤러 호출됨: " + status + " - " + message);
        
        // OAuth2 관련 에러인지 확인
        String continueParam = request.getParameter("continue");
        if (continueParam != null) {
            log.info("OAuth2 인증 오류 발생, 리다이렉트: {}", continueParam);
            return "redirect:/login?error=oauth2&redirectUrl=" + continueParam;
        }
        
        // HTTP 상태에 따른 다른 페이지 반환
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            }
        }
        
        // 기본 에러 페이지
        return "error";
    }
    
    @GetMapping("/error-page")
    public String showErrorPage() {
        return "error";
    }
}

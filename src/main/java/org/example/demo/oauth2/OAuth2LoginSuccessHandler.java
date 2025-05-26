package org.example.demo.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        log.info("OAuth2 로그인 성공! 사용자: {}, 이메일: {}", oAuth2User.getName(), oAuth2User.getEmail());
        
        try {
            // 세션에 사용자 정보 저장 (필요시)
            HttpSession session = request.getSession();
            session.setAttribute("userId", oAuth2User.getId());
            session.setAttribute("userEmail", oAuth2User.getEmail());
            session.setAttribute("userName", oAuth2User.getName());
            
            // 성공 후 리다이렉트 URL 설정
            String targetUrl = determineTargetUrl(request, response, authentication);
            
            log.info("OAuth2 로그인 성공 후 리다이렉트: {}", targetUrl);
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류 발생", e);
            // 오류 발생 시 기본 페이지로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, "/");
        }
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) {
        // 리다이렉트할 URL을 세션에서 가져오거나 기본 URL 사용
        String targetUrl = (String) request.getSession().getAttribute("REDIRECT_URI");
        
        if (targetUrl != null) {
            request.getSession().removeAttribute("REDIRECT_URI");
            return targetUrl;
        }
        
        // 기본 URL로 리다이렉트
        return "/";
    }
}

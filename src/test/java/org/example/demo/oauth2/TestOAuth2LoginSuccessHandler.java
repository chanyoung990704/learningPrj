package org.example.demo.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 테스트 환경용 OAuth2 로그인 성공 핸들러
 * 테스트 시 OAuth2 로그인 성공 후 처리를 단순화합니다.
 */
@Component
@Profile("test")
public class TestOAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public TestOAuth2LoginSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 테스트 환경에서는 기본 동작만 수행
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

package org.example.demo.util;

import org.example.demo.oauth2.CustomOAuth2User;
import org.example.demo.security.CustomUserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SecurityUtils {

    /**
     * Principal 객체로부터 이메일을 추출합니다.
     * @param principal 인증된 사용자 객체 (UserDetails, CustomOAuth2User, OAuth2User 타입 지원)
     * @return 사용자 이메일
     * @throws IllegalArgumentException 지원하지 않는 인증 타입인 경우
     */
    public static String extractEmailFromPrincipal(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getEmail();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getEmail();
        } else if (principal instanceof OAuth2User) {
            return (String) ((OAuth2User) principal).getAttribute("email");
        } else {
            throw new IllegalArgumentException("지원하지 않는 인증 타입입니다: " + principal.getClass().getName());
        }
    }
}

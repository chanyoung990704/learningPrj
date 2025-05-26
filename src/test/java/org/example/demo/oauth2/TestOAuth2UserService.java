package org.example.demo.oauth2;

import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 테스트 환경용 OAuth2 사용자 서비스
 * 실제 OAuth2 인증 없이 테스트를 진행할 수 있도록 도와줍니다.
 */
@Service
@Profile("test")
public class TestOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 테스트 환경에서는 실제 OAuth2 인증 과정을 진행하지 않고 기본 구현을 사용
        return super.loadUser(userRequest);
    }
}

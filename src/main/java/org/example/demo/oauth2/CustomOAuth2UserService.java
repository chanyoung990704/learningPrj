package org.example.demo.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.domain.Role;
import org.example.demo.domain.User;
import org.example.demo.oauth2.provider.GoogleUserInfo;
import org.example.demo.oauth2.provider.KakaoUserInfo;
import org.example.demo.oauth2.provider.NaverUserInfo;
import org.example.demo.oauth2.provider.OAuth2UserInfo;
import org.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("OAuth2 로그인 요청 처리: {}", userRequest.getClientRegistration().getRegistrationId());
            
            // OAuth2 서비스 구분 (google, kakao, naver)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            
            // OAuth2UserInfo 구현체 생성
            OAuth2UserInfo oAuth2UserInfo = null;
            if (registrationId.equals("google")) {
                oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
            } else if (registrationId.equals("kakao")) {
                oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
            } else if (registrationId.equals("naver")) {
                oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
            } else {
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
            }
            
            // 사용자 정보 처리
            String provider = oAuth2UserInfo.getProvider();
            String providerId = oAuth2UserInfo.getProviderId();
            String email = oAuth2UserInfo.getEmail();
            String name = oAuth2UserInfo.getName();
            String profileImage = oAuth2UserInfo.getProfileImage();
            
            // 디버그 로깅 추가
            log.debug("OAuth2 사용자 정보: provider={}, providerId={}, email={}, name={}", 
                    provider, providerId, email, name);
            
            // 이메일이 없는 경우 처리 (소셜 로그인 제공자 + ID로 이메일 생성)
            if (email == null || email.isEmpty()) {
                email = provider + "_" + providerId + "@sociallogin.com";
            }
            
            // DB에 해당 사용자가 있는지 확인
            User user = userRepository.findByEmail(email).orElse(null);
            
            // 사용자가 없으면 새로 생성
            if (user == null) {
                // 랜덤 비밀번호 생성 (실제 사용되지 않음)
                String password = passwordEncoder.encode(UUID.randomUUID().toString());
                
                user = User.builder()
                        .email(email)
                        .password(password)
                        .name(name)
                        .role(Role.ROLE_USER)
                        .provider(provider)
                        .providerId(providerId)
                        .socialAccount(true)
                        .profileImage(profileImage)
                        .build();
                
                userRepository.save(user);
                log.info("새로운 OAuth2 사용자 등록: {}", email);
            } else {
                // 기존 사용자가 있지만 소셜 계정이 아닌 경우, 소셜 정보 업데이트
                if (!user.isSocialAccount() || !provider.equals(user.getProvider())) {
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    user.setSocialAccount(true);
                    
                    // 프로필 이미지가 있으면 업데이트
                    if (profileImage != null && !profileImage.isEmpty()) {
                        user.setProfileImage(profileImage);
                    }
                    
                    userRepository.save(user);
                    log.info("기존 사용자의 OAuth2 정보 업데이트: {}", email);
                }
            }
            
            // OAuth2User 정보와 사용자 객체를 포함한 인증 객체 반환
            return new CustomOAuth2User(
                    oAuth2User.getAuthorities(),
                    oAuth2User.getAttributes(),
                    userRequest.getClientRegistration().getProviderDetails()
                            .getUserInfoEndpoint().getUserNameAttributeName(),
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole()
            );
        } catch (Exception e) {
            // 모든 예외를 로깅하고 OAuth2AuthenticationException으로 래핑
            log.error("OAuth2 로그인 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

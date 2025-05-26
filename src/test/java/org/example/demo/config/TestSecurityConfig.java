package org.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Test 환경에서 사용하는 Security 설정
 * OAuth2 관련 빈을 필요로 하지 않으며, 테스트를 위해 간소화된 설정을 제공합니다.
 */
@Configuration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (테스트 환경)
                .authorizeHttpRequests(authz -> authz
                        // 게시글 조회 및 검색 허용
                        .requestMatchers("/**").permitAll() // 테스트 환경에서는 모든 요청 허용
                )
                // 폼 로그인 설정
                .formLogin(login ->
                        login.loginPage("/login")
                                .defaultSuccessUrl("/", false)
                                .permitAll())
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                                .permitAll())
                .httpBasic(withDefaults());

        return http.build();
    }
}

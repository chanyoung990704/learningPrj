package org.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.example.demo.oauth2.CustomOAuth2UserService;
import org.example.demo.oauth2.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Profile({"dev", "prod"}) 
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(authz -> authz
                        // 게시글 조회 및 검색 허용
                        .requestMatchers(HttpMethod.GET, "/posts/{id:[0-9]+}", "/posts", "/posts/search").permitAll()
                        // 게시글 내 이미지 조회 허용
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/images/{filename}").permitAll()
                        // 파일 다운로드는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/files/{id}").authenticated()
                        // 공개 URL
                        .requestMatchers("/", "/register", "/error", "/error-page", "/oauth2-info").permitAll()
                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(login ->
                        login.loginPage("/login")
                                .defaultSuccessUrl("/", false)
                                .permitAll())
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            // OAuth2 로그인 실패 시 로깅
                            System.out.println("OAuth2 로그인 실패: " + exception.getMessage());
                            // 로그인 페이지로 리다이렉트
                            response.sendRedirect("/login?error=oauth2");
                        })
                        .permitAll())
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                                .permitAll())
                .httpBasic(withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 시 로그인 페이지로 리다이렉트
                            response.sendRedirect("/login?error=auth");
                        })
                        .accessDeniedPage("/error-page"));

        return http.build();
    }
}

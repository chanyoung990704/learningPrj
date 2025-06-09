package org.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.oauth2.CustomOAuth2UserService;
import org.example.demo.oauth2.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Profile({"dev", "prod"})
@Slf4j
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 설정 - 파일 업로드/다운로드 경로 제외
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/posts/uploads/images/**", "/posts/uploads/files/**"))

                // 요청 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // 파일 다운로드 경로 - 인증 필요(이미지 제외)
                        .requestMatchers(GET, "/posts/uploads/files/**")
                        .authenticated()

                        // 이미지 본문 표시 경로 - 인증 필요 없음
                        .requestMatchers(GET, "/posts/uploads/images/**")
                        .permitAll()

                        // 게시글 조회 - 공개 허용
                        .requestMatchers(GET, "/posts/{category:[a-z]+}/{id:[0-9]+}", "/posts/{category:[a-z]+}")
                        .permitAll()

                        // 공개 페이지
                        .requestMatchers("/", "/register", "/error", "/error-page", "/oauth2-info")
                        .permitAll()

                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**")
                        .permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest()
                        .authenticated()
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .successHandler(this::handleLoginSuccess)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(this::handleOAuth2Failure)
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=success")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessHandler(this::handleLogoutSuccess)
                        .permitAll()
                )

                // HTTP Basic 인증
                .httpBasic(withDefaults())

                // 예외 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this::handleAuthenticationException)
                        .accessDeniedPage("/error-page")
                )

                .build();
    }

    // 로그인 성공 핸들러를 별도 메서드로 분리
    private void handleLoginSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException {
        HttpSession session = request.getSession(false);
        String redirectUrl = Optional.ofNullable(session)
                .map(s -> (String) s.getAttribute("REDIRECT_URI"))
                .orElse("/");

        if (session != null) {
            session.removeAttribute("REDIRECT_URI");
        }

        response.sendRedirect(redirectUrl);
    }

    // OAuth2 실패 핸들러를 별도 메서드로 분리
    private void handleOAuth2Failure(HttpServletRequest request,
                                     HttpServletResponse response,
                                     AuthenticationException exception) throws IOException {
        log.warn("OAuth2 로그인 실패: {}", exception.getMessage());
        response.sendRedirect("/login?error=oauth2");
    }

    // 로그아웃 성공 핸들러를 별도 메서드로 분리
    private void handleLogoutSuccess(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/?logout=success");
    }

    // 인증 예외 핸들러를 별도 메서드로 분리
    private void handleAuthenticationException(HttpServletRequest request,
                                               HttpServletResponse response,
                                               AuthenticationException authException) throws IOException {
        String error = request.getParameter("error");
        String redirectUrl = "auth".equals(error) ? "/login" : "/login?error=auth";
        response.sendRedirect(redirectUrl);
    }
}
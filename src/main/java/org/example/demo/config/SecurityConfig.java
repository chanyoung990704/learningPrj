package org.example.demo.config;

import jakarta.servlet.http.HttpSession;
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
                .authorizeHttpRequests(authz -> authz
                        // 기존 경로 설정 유지
                        .requestMatchers(HttpMethod.GET, "/posts/{id:[0-9]+}", "/posts", "/posts/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/images/{filename}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/files/{id}").authenticated()
                        .requestMatchers("/", "/register", "/error", "/error-page", "/oauth2-info").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정 수정
                .formLogin(login ->
                        login.loginPage("/login")
                                .defaultSuccessUrl("/", true) // 항상 성공 URL로 리다이렉트
                                .successHandler((request, response, authentication) -> {
                                    // 로그인 성공 후 세션에서 리다이렉트 URL 확인
                                    HttpSession session = request.getSession(false);
                                    String redirectUrl = session != null ? (String) session.getAttribute("REDIRECT_URI") : null;

                                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                                        session.removeAttribute("REDIRECT_URI");
                                        response.sendRedirect(redirectUrl);
                                    } else {
                                        response.sendRedirect("/");
                                    }
                                })
                                .failureUrl("/login?error=true") // 로그인 실패 시 URL
                                .permitAll()
                )
                // 나머지 설정은 유지
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            System.out.println("OAuth2 로그인 실패: " + exception.getMessage());
                            response.sendRedirect("/login?error=oauth2");
                        })
                        .permitAll())
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                                .permitAll())
                .httpBasic(withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/login?error=auth");
                        })
                        .accessDeniedPage("/error-page"));

        return http.build();
    }
}
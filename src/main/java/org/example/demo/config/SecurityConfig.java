package org.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(authz -> authz
                        // 게시글 조회 및 검색 허용
                        .requestMatchers(HttpMethod.GET, "/posts/{id:[0-9]+}", "/posts", "/posts/search").permitAll()
                        // 게시글 내 이미지 조회 허용
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/images/{filename}").permitAll()
                        // 파일 다운로드는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/posts/uploads/files/{id}").authenticated()
                        // 공개 URL
                        .requestMatchers("/", "/register").permitAll()
                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
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


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

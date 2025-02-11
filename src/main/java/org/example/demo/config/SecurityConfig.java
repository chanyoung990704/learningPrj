package org.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/register").permitAll() // 공개 URL
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // css 파일
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin(login ->
                        login.loginPage("/login")
                                .defaultSuccessUrl("/")
                                .permitAll())
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                                .permitAll())
                .httpBasic(withDefaults()); // HTTP Basic 인증 활성화

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

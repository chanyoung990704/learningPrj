package org.example.demo.security;

import lombok.Getter;
import org.example.demo.domain.Role;
import org.example.demo.oauth2.CustomOAuth2User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    // For regular user authentication
    public CustomUserDetails(Long id, String email, String password, String name, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.getRole())
        );
    }

    // For OAuth2 user
    public CustomUserDetails(CustomOAuth2User oauth2User) {
        this.id = oauth2User.getId();
        this.email = oauth2User.getEmail();
        this.password = null; // OAuth2 users don't have passwords
        this.name = oauth2User.getName();
        this.role = oauth2User.getRole();
        this.authorities = oauth2User.getAuthorities();
        this.attributes = oauth2User.getAttributes();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Override
    public String getName() {
        return name;
    }
}

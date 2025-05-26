package org.example.demo.oauth2;

import lombok.Getter;
import org.example.demo.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    
    private final Long id;
    private final String email;
    private final String name;
    private final Role role;
    
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes,
                           String nameAttributeKey,
                           Long id,
                           String email,
                           String name,
                           Role role) {
        super(authorities, attributes, nameAttributeKey);
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}

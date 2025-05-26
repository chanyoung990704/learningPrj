package org.example.demo.oauth2.provider;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
    
    private Map<String, Object> attributes;
    private Map<String, Object> response;
    
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }
    
    @Override
    public String getProvider() {
        return "naver";
    }
    
    @Override
    public String getProviderId() {
        return (String) response.get("id");
    }
    
    @Override
    public String getEmail() {
        return (String) response.get("email");
    }
    
    @Override
    public String getName() {
        return (String) response.get("name");
    }
    
    @Override
    public String getProfileImage() {
        return (String) response.get("profile_image");
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

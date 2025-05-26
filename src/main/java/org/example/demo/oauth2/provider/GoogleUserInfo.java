package org.example.demo.oauth2.provider;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
    
    private Map<String, Object> attributes;
    
    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getProvider() {
        return "google";
    }
    
    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
    }
    
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
    
    @Override
    public String getProfileImage() {
        return (String) attributes.get("picture");
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

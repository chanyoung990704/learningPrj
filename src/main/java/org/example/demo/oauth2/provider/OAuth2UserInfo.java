package org.example.demo.oauth2.provider;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
    String getProfileImage();
    Map<String, Object> getAttributes();
}

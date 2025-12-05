package com.fc.authservice.dto;

import com.fc.authservice.enums.SocialProvider;
import lombok.Data;

@Data
public class SocialLoginRequest {
    private SocialProvider provider;   // GOOGLE or FACEBOOK
    private String email;
    private String name;
    private String avatar;            // profile image URL (optional)
    private String providerUserId;    // google uid / facebook id
    private String idToken;           // optional: for Google token verification
    private String accessToken;       // optional: for Facebook token verification
}
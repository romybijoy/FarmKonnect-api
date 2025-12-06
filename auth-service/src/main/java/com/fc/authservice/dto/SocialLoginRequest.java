package com.fc.authservice.dto;

import com.fc.authservice.enums.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used for social login or signup through supported providers
 * such as Google or Facebook. This payload contains the provider details,
 * basic user profile information, and tokens/IDs required for verification.
 * Fields:
 * - provider: Identifies the social platform (e.g., GOOGLE, FACEBOOK)
 * - email: Email associated with the social account
 * - name: Display name of the user
 * - avatar: Optional profile image URL
 * - providerUserId: Unique ID provided by the social platform (Google UID / Facebook ID)
 * - idToken: Google ID token used for verification (optional)
 * - accessToken: Facebook access token used for verification (optional)
 * Used in the /auth/social-login endpoint to authenticate or register users.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    /** The social platform used for authentication (GOOGLE or FACEBOOK) */
    private SocialProvider provider;

    /** Email associated with the social account */
    private String email;

    /** Full display name of the user */
    private String name;

    /** Optional profile image URL */
    private String avatar;

    /** Unique user ID provided by the social platform (Google UID / Facebook ID) */
    private String providerUserId;

    /** Google ID token used for secure token verification (optional) */
    private String idToken;

    /** Facebook access token used for secure verification (optional) */
    private String accessToken;
}
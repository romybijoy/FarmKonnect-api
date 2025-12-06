package com.fc.authservice.enums;

/**
 * Enumeration representing the supported social authentication providers.
 * Used during social login/signup to identify the platform from which
 * the authentication request originates.
 * Providers:
 * - GOOGLE   : Google OAuth2 authentication
 * - FACEBOOK : Facebook OAuth2 authentication
 * Additional providers (e.g., APPLE, GITHUB) can be added as needed.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
public enum SocialProvider {
    GOOGLE,
    FACEBOOK
}